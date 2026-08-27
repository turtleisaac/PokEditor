package io.github.turtleisaac.pokeditor.utilities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class reimplements the game's own generator, so its properties come from the algorithm's
 * definition rather than from what the Java happens to produce today.
 * <p>
 * The generator is the 32-bit LCG {@code s' = (0x41C64E6D * s + 0x6073) mod 2^32}. Every
 * expectation below is computed from that definition with exact arithmetic, deliberately not by
 * repeating the production expression, so a sign-extension or overflow mistake in the shipped
 * masking cannot agree with the oracle.
 */
class TrainerPersonalityCalculatorTest
{
    private static final BigInteger MULTIPLIER = BigInteger.valueOf(0x41C64E6DL);
    private static final BigInteger INCREMENT = BigInteger.valueOf(0x6073L);
    private static final BigInteger MODULUS = BigInteger.ONE.shiftLeft(32);

    /** The next state of the LCG, computed exactly, independently of how the code does it. */
    private static long nextState(long seed)
    {
        return MULTIPLIER.multiply(BigInteger.valueOf(seed)).add(INCREMENT).mod(MODULUS).longValueExact();
    }

    @ParameterizedTest(name = "seed {0}")
    @ValueSource(longs = {0L, 1L, 12345L, 0x12345678L, 0x7FFFFFFFL, 0x80000000L, 0xFFFFFFFFL})
    @DisplayName("each draw is the LCG's next state, computed in exact arithmetic")
    void drawsFollowTheLcgDefinition(long seed)
    {
        TrainerPersonalityCalculator.setRandom(seed);

        long state = seed;
        for (int step = 0; step < 25; step++)
        {
            state = nextState(state);
            assertThat(TrainerPersonalityCalculator.random())
                    .as("draw %d from seed %d", step, seed)
                    .isEqualTo(state);
        }
    }

    /**
     * The generator is 32 bits wide. Anything outside that range means a shift or a mask has
     * escaped, and every downstream consumer here slices 16-bit halves out of the result.
     */
    @Test
    @DisplayName("every draw stays inside 32 unsigned bits")
    void drawsStayWithinThirtyTwoBits()
    {
        TrainerPersonalityCalculator.setRandom(0x9E3779B9L);

        for (int i = 0; i < 5_000; i++)
        {
            long draw = TrainerPersonalityCalculator.random();
            assertThat(draw).as("draw %d", i).isBetween(0L, 0xFFFFFFFFL);
        }
    }

    /**
     * Determinism is the whole point of reproducing the game's generator: a given seed has to
     * replay the same sequence, otherwise a PID computed here cannot be reproduced in-game.
     */
    @Test
    @DisplayName("the same seed replays the same sequence")
    void sameSeedProducesSameSequence()
    {
        long[] first = drawMany(0xDEADBEEFL, 100);
        long[] second = drawMany(0xDEADBEEFL, 100);

        assertThat(second).isEqualTo(first);
    }

    /**
     * ...and a different seed must not. Without this, a generator stuck on a constant would
     * satisfy the determinism property above.
     */
    @Test
    @DisplayName("different seeds produce different sequences")
    void differentSeedsDiverge()
    {
        assertThat(drawMany(1L, 20)).isNotEqualTo(drawMany(2L, 20));
    }

    private static long[] drawMany(long seed, int count)
    {
        TrainerPersonalityCalculator.setRandom(seed);
        long[] draws = new long[count];
        for (int i = 0; i < count; i++)
            draws[i] = TrainerPersonalityCalculator.random();
        return draws;
    }

    /**
     * The trainer ID pair is a packed 32-bit value: TID in bits 16-31 and SID in bits 0-15, and
     * the callers unpack it with exactly those masks. Splicing a 32-bit draw into the high half
     * rather than that draw's own 16-bit slice pushes the result up to 48 bits, which then makes
     * {@code (id >> 16) & 0xffff} read a mixture of two draws instead of a TID.
     */
    @Test
    @DisplayName("a generated ID pair fits in 32 bits, so TID and SID are each 16 bits")
    void idPairFitsInThirtyTwoBits()
    {
        TrainerPersonalityCalculator.setRandom(0x1234L);

        for (int i = 0; i < 2_000; i++)
        {
            long id = TrainerPersonalityCalculator.rndFlagCall();

            assertThat(id).as("packed id %d", i).isBetween(0L, 0xFFFFFFFFL);
            assertThat((id >> 16) & 0xffff).as("TID of id %d", i).isBetween(0L, 65535L);
            assertThat(id & 0xffff).as("SID of id %d", i).isBetween(0L, 65535L);
        }
    }

    /**
     * Each half of the ID pair is its own draw's high 16 bits, in order: TID from the first draw,
     * SID from the second. Stated against draws taken independently from the same seed, so it
     * pins which bits of which draw land where.
     */
    @Test
    @DisplayName("the two halves of an ID pair are the high 16 bits of two consecutive draws")
    void idPairHalvesComeFromConsecutiveDraws()
    {
        long seed = 0xABCDEF01L;

        TrainerPersonalityCalculator.setRandom(seed);
        long firstDraw = TrainerPersonalityCalculator.random();
        long secondDraw = TrainerPersonalityCalculator.random();

        TrainerPersonalityCalculator.setRandom(seed);
        long id = TrainerPersonalityCalculator.rndFlagCall();

        assertThat((id >> 16) & 0xffff).as("TID").isEqualTo((firstDraw >> 16) & 0xffff);
        assertThat(id & 0xffff).as("SID").isEqualTo((secondDraw >> 16) & 0xffff);
    }

    /**
     * An ID pair costs exactly two draws. The generator state is shared and the game advances it
     * in lockstep, so consuming a different number of draws desynchronises everything after it.
     */
    @Test
    @DisplayName("generating an ID pair advances the generator by exactly two draws")
    void idPairConsumesTwoDraws()
    {
        TrainerPersonalityCalculator.setRandom(777L);
        TrainerPersonalityCalculator.rndFlagCall();
        long afterIdPair = TrainerPersonalityCalculator.random();

        TrainerPersonalityCalculator.setRandom(777L);
        TrainerPersonalityCalculator.random();
        TrainerPersonalityCalculator.random();
        long afterTwoDraws = TrainerPersonalityCalculator.random();

        assertThat(afterIdPair).isEqualTo(afterTwoDraws);
    }

    /**
     * A PID here is built as a 16-bit slice scaled by 256 plus a gender constant, so it occupies
     * at most 24 bits and is never negative. A negative or wider value means the arithmetic
     * overflowed the int it is returned in, and the value would be written back out truncated.
     */
    @Test
    @DisplayName("a generated PID is non-negative and fits in 24 bits")
    void generatedPidFitsInTwentyFourBits()
    {
        for (int difficulty = 0; difficulty <= 65535; difficulty += 97)
        {
            int pid = TrainerPersonalityCalculator.generatePid(320, 3, true, 466, 50, difficulty, 0, false);
            assertThat(pid).as("PID for difficulty %d", difficulty).isBetween(0, 0xFFFFFF);
        }
    }

    /**
     * generatePid seeds the generator itself, so it is a pure function of its arguments. If it
     * ever leaked the ambient generator state the same trainer would get a different PID
     * depending on what the tool happened to do beforehand.
     */
    @Test
    @DisplayName("a PID depends only on its arguments, not on the generator state it inherits")
    void generatedPidIgnoresAmbientGeneratorState()
    {
        TrainerPersonalityCalculator.setRandom(0L);
        int fromZero = TrainerPersonalityCalculator.generatePid(320, 4, true, 466, 50, 2500, 0, false);

        TrainerPersonalityCalculator.setRandom(0xFFFFFFFFL);
        for (int i = 0; i < 13; i++)
            TrainerPersonalityCalculator.random();
        int fromElsewhere = TrainerPersonalityCalculator.generatePid(320, 4, true, 466, 50, 2500, 0, false);

        assertThat(fromElsewhere).isEqualTo(fromZero);
    }

    /**
     * The search domain is the whole 16-bit difficulty range, 0 to 65535 inclusive. These
     * arguments were chosen because 65535 is the only value in that domain which produces this
     * PID, so a search bounded by {@code i < 65535} has nowhere else to land and returns -1.
     * The target is derived from 65535 rather than hard-coded, so the witness cannot rot into a
     * recording of a number.
     */
    @Test
    @DisplayName("the brute force search reaches the last value of its domain, 65535")
    void bruteForceCoversTheTopOfItsDomain()
    {
        int trainerIdx = 320, trainerClassIdx = 1, speciesIdx = 466, level = 50;

        int target = TrainerPersonalityCalculator.generatePid(trainerIdx, trainerClassIdx, true, speciesIdx, level, 65535, 0, false);

        assertThat(TrainerPersonalityCalculator.bruteForcePid(target, trainerIdx, trainerClassIdx, true, speciesIdx, level))
                .as("the difficulty value which produces PID %d", target)
                .isEqualTo(65535);
    }

    /**
     * Whatever index comes back must actually reproduce the PID that was searched for - the
     * search is only useful if its answer round trips.
     */
    @Test
    @DisplayName("a found difficulty value reproduces the PID that was searched for")
    void bruteForceResultReproducesTheTarget()
    {
        int trainerIdx = 100, trainerClassIdx = 2, speciesIdx = 25, level = 30;

        for (int difficulty : new int[] {0, 1, 255, 4096, 32768, 65534, 65535})
        {
            int target = TrainerPersonalityCalculator.generatePid(trainerIdx, trainerClassIdx, true, speciesIdx, level, difficulty, 0, false);
            int found = TrainerPersonalityCalculator.bruteForcePid(target, trainerIdx, trainerClassIdx, true, speciesIdx, level);

            assertThat(found).as("search for the PID of difficulty %d", difficulty).isNotEqualTo(-1);
            assertThat(TrainerPersonalityCalculator.generatePid(trainerIdx, trainerClassIdx, true, speciesIdx, level, found, 0, false))
                    .as("PID regenerated from the difficulty value the search returned for %d", difficulty)
                    .isEqualTo(target);
        }
    }

    /**
     * A PID no difficulty value can produce has to be reported as not found, rather than the
     * search returning whatever index it stopped on. Every PID here is at least 120, so 0 is
     * unreachable by construction.
     */
    @Test
    @DisplayName("an unreachable PID is reported as not found")
    void bruteForceReportsAnUnreachableTarget()
    {
        assertThat(TrainerPersonalityCalculator.bruteForcePid(0, 320, 1, true, 466, 50)).isEqualTo(-1);
    }
}
