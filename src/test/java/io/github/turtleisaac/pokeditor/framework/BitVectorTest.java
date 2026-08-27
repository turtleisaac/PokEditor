package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * A bit vector is defined by one property above all others: each index names its own bit and
 * nothing else. Every test here is an instance of that, because the ways this class has broken
 * (a mask of {@code idx} instead of {@code 1L << idx}, an allocation sized by truncating
 * division) are all failures of one index to keep out of another index's business.
 */
class BitVectorTest
{
    /**
     * Independence. Setting bit i must leave every other bit clear - not "roughly the right
     * bits". A mask of {@code idx} rather than {@code 1L << (idx % 64)} makes setBit(3) light
     * up bits 0 and 1 as well, which no assertion about bit 3 alone would ever notice.
     */
    @Test
    @DisplayName("setting bit i leaves bit j set if and only if i == j")
    void setBitAffectsOnlyThatBit()
    {
        int size = 200;
        for (int i = 0; i < size; i++)
        {
            BitVector vector = new BitVector(size);
            vector.setBit(i);

            for (int j = 0; j < size; j++)
            {
                assertThat(vector.isSet(j))
                        .withFailMessage("after setBit(%d), isSet(%d) was %s", i, j, vector.isSet(j))
                        .isEqualTo(i == j);
            }
        }
    }

    /**
     * A freshly built vector holds nothing. Without this the independence test above could be
     * satisfied by a vector which reports every bit as set.
     */
    @Test
    @DisplayName("a new vector has no bits set")
    void newVectorIsEmpty()
    {
        BitVector vector = new BitVector(200);
        for (int i = 0; i < 200; i++)
            assertThat(vector.isSet(i)).as("bit %d of a new vector", i).isFalse();
    }

    /**
     * Capacity. {@code new BitVector(n)} promises n usable bits; sizing the backing array with
     * {@code n / 64} instead of a rounding-up division makes every index in the final partial
     * word blow up with an ArrayIndexOutOfBoundsException.
     */
    @ParameterizedTest(name = "a vector of {0} bits addresses all of 0..{0}-1")
    @ValueSource(ints = {1, 2, 63, 64, 65, 100, 127, 128, 129, 255, 256, 1000})
    @DisplayName("a vector of n bits can address every index from 0 to n-1")
    void everyDeclaredIndexIsAddressable(int size)
    {
        BitVector vector = new BitVector(size);

        for (int i = 0; i < size; i++)
        {
            final int idx = i;
            assertThat(vector.isSet(idx)).as("isSet(%d) of %d bits", idx, size).isFalse();
            vector.setBit(idx);
            assertThat(vector.isSet(idx)).as("setBit(%d) of %d bits", idx, size).isTrue();
        }

        // and all of them at once, so the last word is not merely reachable but distinct
        for (int i = 0; i < size; i++)
            assertThat(vector.isSet(i)).as("bit %d after setting all %d", i, size).isTrue();
    }

    /**
     * set and clear are inverses, and clearing is as narrow as setting. Starting from a fully
     * populated vector means a clear which reached into a neighbouring bit shows up immediately.
     */
    @Test
    @DisplayName("clearing bit i undoes setting bit i and touches nothing else")
    void clearBitIsTheInverseOfSetBit()
    {
        int size = 130;
        for (int i = 0; i < size; i++)
        {
            BitVector vector = new BitVector(size);
            for (int j = 0; j < size; j++)
                vector.setBit(j);

            vector.clearBit(i);

            for (int j = 0; j < size; j++)
            {
                assertThat(vector.isSet(j))
                        .withFailMessage("after clearBit(%d) on a full vector, isSet(%d) was %s", i, j, vector.isSet(j))
                        .isEqualTo(i != j);
            }

            vector.setBit(i);
            assertThat(vector.isSet(i)).as("setBit after clearBit restores bit %d", i).isTrue();
        }
    }

    /**
     * Idempotence - setting a bit twice is the same as setting it once, and likewise for clear.
     * A mask built by addition rather than a bitwise or would break here.
     */
    @Test
    @DisplayName("setting or clearing the same bit twice is the same as doing it once")
    void repeatedOperationsAreIdempotent()
    {
        BitVector twice = new BitVector(128);
        BitVector once = new BitVector(128);

        twice.setBit(70);
        twice.setBit(70);
        once.setBit(70);

        assertThat(twice.toLongs()).isEqualTo(once.toLongs());

        twice.clearBit(70);
        twice.clearBit(70);
        once.clearBit(70);

        assertThat(twice.toLongs()).isEqualTo(once.toLongs());
    }

    /**
     * toLongs must hand back a snapshot, not the live array - a caller who is handed the
     * internals can scribble on the vector by accident.
     */
    @Test
    @DisplayName("toLongs returns a copy that later writes do not alter")
    void toLongsIsDefensivelyCopied()
    {
        BitVector vector = new BitVector(64);
        long[] before = vector.toLongs();

        vector.setBit(5);

        assertThat(before).as("snapshot taken before setBit(5)").containsOnly(0L);
        assertThat(vector.toLongs()).as("live state after setBit(5)").isNotEqualTo(before);
    }

    /**
     * Out-of-range indices must be reported, not silently redirected onto some other bit.
     * {@code 1L << (-1 % 64)} is {@code 1L << -1}, which Java evaluates as {@code 1L << 63} -
     * so setBit(-1) quietly sets the top bit of word 0, corrupting a perfectly valid bit.
     * The same argument applies at the top end: a vector of 100 bits owns indices 0..99, and
     * index 100 happens to land in the unused slack of the second word.
     * <p>
     * Both ends are checked softly so a failure reports every case, not just the first.
     */
    @Test
    @DisplayName("an index outside 0..n-1 is rejected instead of silently writing some other bit")
    void outOfRangeIndicesAreRejected()
    {
        assertSoftly(softly -> {
            BitVector low = new BitVector(64);
            try {
                low.setBit(-1);
            }
            catch (RuntimeException expected) {
                // rejecting outright is a perfectly good answer
            }
            softly.assertThat(low.toLongs())
                    .as("a vector of 64 bits after an attempted setBit(-1) - no valid bit may have been written")
                    .containsOnly(0L);

            BitVector high = new BitVector(100);
            try {
                high.setBit(100);
            }
            catch (RuntimeException expected) {
                // likewise
            }
            softly.assertThat(high.toLongs())
                    .as("a vector of 100 bits after an attempted setBit(100) - index 100 is not part of the vector")
                    .containsOnly(0L);
        });
    }

    /**
     * An index past the end of the backing store must fail loudly rather than being folded back
     * onto a live bit by the modulo in the mask.
     */
    @Test
    @DisplayName("an index far past the end of the vector throws rather than wrapping around")
    void wildIndexThrows()
    {
        BitVector vector = new BitVector(64);

        assertThatThrownBy(() -> vector.setBit(1_000))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }
}
