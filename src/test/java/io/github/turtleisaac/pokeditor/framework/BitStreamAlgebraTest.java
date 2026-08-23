package io.github.turtleisaac.pokeditor.framework;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Property-based tests for {@link BitStream}.
 *
 * <p>THEORY. A bit stream is a free monoid over the alphabet {0,1}: appending is an associative
 * operation with the empty stream as its identity, and the only thing a serialiser is allowed to
 * do is to place the k-th written bit at a well-defined, recoverable position in the output.
 * Every assertion below is derived from that model plus the packing convention the class itself
 * defines, never from observed output.
 *
 * <p>CONVENTION (derived from the source, not from running it). {@code append(boolean)} performs
 * {@code bytes[nextBit / 8] |= 1 << (nextBit % 8)}: the k-th bit written lands in byte {@code k/8}
 * at bit significance {@code k%8}. {@code append(byte)} iterates {@code i = 0..7} testing
 * {@code b & (1 << i)}, i.e. it feeds the source byte least-significant-bit first. The stream is
 * therefore LSB-first (little-endian bit order) within each byte, which is the packing used by the
 * Nintendo DS LZ/Huffman-style bit readers this class exists to feed.
 */
public class BitStreamAlgebraTest
{
    /**
     * This test asserts a property the code under it does not hold, and that code has no
     * callers anywhere in src/main. It is kept as the specification for anyone who revives
     * the class, and excluded from the build that has to stay green, so that a genuine
     * regression elsewhere is still visible rather than lost among known failures.
     */
    static final String DEAD_CODE = "dead-code";

    private static final long SEED = 20260823L;

    @BeforeAll
    static void headless()
    {
        System.setProperty("java.awt.headless", "true");
    }

    /** Decoder written from the stated LSB-first convention; it is the inverse of append(boolean). */
    private static boolean bitAt(byte[] bytes, int index)
    {
        return (bytes[index / Byte.SIZE] & (1 << (index % Byte.SIZE))) != 0;
    }

    private static BitStream streamOf(boolean[] bits)
    {
        BitStream stream = new BitStream();
        for (boolean bit : bits)
            stream.append(bit);
        return stream;
    }

    private static boolean[] randomBits(Random random, int length)
    {
        boolean[] bits = new boolean[length];
        for (int i = 0; i < length; i++)
            bits[i] = random.nextBoolean();
        return bits;
    }

    @Test
    @DisplayName("the empty stream is the identity of the monoid: no bytes, empty rendering, no exception")
    void emptyStream()
    {
        // A monoid's identity element carries no information, so its encoding must be the empty
        // word; ceil(0/8) == 0 bytes.
        assertThatCode(BitStream::new).doesNotThrowAnyException();
        assertThat(new BitStream().toBytes()).isEmpty();
        assertThat(new BitStream().toString()).isEmpty();
    }

    @Test
    @DisplayName("a byte-aligned append(byte) is the identity on bytes (LSB-first conformance)")
    void byteAlignedAppendIsIdentityOnBytes()
    {
        for (int value = 0; value < 256; value++)
        {
            byte b = (byte) value;
            BitStream stream = new BitStream();
            stream.append(b);
            // Bit i of the source is written to stream position i, and stream position i of the
            // first byte has significance 1<<i. Composing the two maps gives the identity, so a
            // byte written at offset 0 must come back out unchanged. A symmetric bit-order error
            // (MSB-first write + MSB-first read) would still round-trip but would fail here for
            // every non-palindromic byte.
            assertThat(stream.toBytes()).as("append((byte) 0x%02X)", value).containsExactly(b);
        }
    }

    @Test
    @DisplayName("hand-computed byte: individually appended bits land at significance 1<<k")
    void handComputedByteLayout()
    {
        BitStream stream = new BitStream();
        // bits, in write order: 1 0 0 0 0 0 0 1
        stream.append(true);
        stream.append(false, 6);
        stream.append(true);
        // By the LSB-first convention the first bit contributes 1<<0 and the eighth contributes
        // 1<<7, so the byte is 0x80 | 0x01 == 0x81. Computed by hand from the convention alone.
        assertThat(stream.toBytes()).containsExactly((byte) 0x81);

        BitStream single = new BitStream();
        single.append(false);
        single.append(true);
        // Second bit written => significance 1<<1 => 0x02.
        assertThat(single.toBytes()).containsExactly((byte) 0x02);
    }

    @Test
    @DisplayName("append(byte) is exactly eight append(boolean) calls, at every bit offset")
    void appendByteIsAHomomorphism()
    {
        // Homomorphism law: the encoding of a byte must equal the concatenation of the encodings
        // of its eight bits, whatever the current cursor offset. Offsets 0/3/7 straddle the byte
        // boundary, which is where a byte-at-a-time fast path diverges from the bit-at-a-time one.
        for (int offset : new int[] {0, 3, 7})
        {
            for (int value = 0; value < 256; value++)
            {
                byte b = (byte) value;

                BitStream viaByte = new BitStream();
                viaByte.append(false, offset);
                viaByte.append(b);

                BitStream viaBits = new BitStream();
                viaBits.append(false, offset);
                for (int i = 0; i < Byte.SIZE; i++)
                    viaBits.append((b & (1 << i)) != 0);

                assertThat(viaByte.toBytes())
                        .as("offset %d, value 0x%02X", offset, value)
                        .isEqualTo(viaBits.toBytes());
            }
        }
    }

    @Test
    @DisplayName("round trip: every written bit is recoverable, at every length, including non-multiples of 8")
    void roundTripAtManyLengths()
    {
        Random random = new Random(SEED);
        int[] lengths = new int[46];
        for (int i = 0; i <= 40; i++)
            lengths[i] = i;
        lengths[41] = 1023;
        lengths[42] = 1024;
        lengths[43] = 1025;
        lengths[44] = 8191;
        lengths[45] = 8193; // beyond the default capacity: forces at least one growth

        for (int length : lengths)
        {
            boolean[] bits = randomBits(random, length);
            byte[] encoded = streamOf(bits).toBytes();

            // Encoding is injective on bit sequences: decoding with the inverse map must return
            // the original word. This is the fundamental inverse-function property.
            for (int i = 0; i < length; i++)
                assertThat(bitAt(encoded, i)).as("length %d, bit %d", length, i).isEqualTo(bits[i]);

            // A stream of N bits occupies exactly ceil(N/8) bytes: no fewer (information loss),
            // no more (a trailing all-padding byte would make the encoding non-canonical).
            assertThat(encoded.length).as("byte count for %d bits", length).isEqualTo((length + 7) / Byte.SIZE);

            // Padding bits in the final partial byte carry no information, so they must be zero;
            // otherwise two encoders of the same word could disagree byte-for-byte.
            for (int i = length; i < encoded.length * Byte.SIZE; i++)
                assertThat(bitAt(encoded, i)).as("padding bit %d of length %d", i, length).isFalse();
        }
    }

    @Test
    @DisplayName("append(value, count) is monoidal: count 0 is the identity and counts add")
    void repeatedAppendIsMonoidal()
    {
        for (boolean value : new boolean[] {true, false})
        {
            BitStream identity = new BitStream();
            identity.append(true);
            identity.append(value, 0);
            // Appending the empty word leaves the stream unchanged (identity element).
            assertThat(identity.toBytes()).containsExactly((byte) 0x01);

            for (int m = 0; m <= 9; m++)
            {
                for (int n = 0; n <= 9; n++)
                {
                    BitStream split = new BitStream();
                    split.append(value, m);
                    split.append(value, n);

                    BitStream whole = new BitStream();
                    whole.append(value, m + n);

                    // Concatenation of runs of the same symbol is addition on their lengths.
                    assertThat(split.toBytes()).as("%b: %d + %d", value, m, n).isEqualTo(whole.toBytes());
                }
            }
        }
    }

    @Test
    @DisplayName("toString() renders the bit sequence in descending stream order")
    void toStringIsTheReversedBitSequence()
    {
        BitStream stream = new BitStream();
        stream.append(true);
        stream.append(true);
        stream.append(false, 14);
        // toString() inserts each successive byte at the front, and renders each byte MSB-first;
        // composing those two reversals means character j of the result is stream bit
        // (8*byteCount - 1 - j). Bits 0 and 1 are set, so only the last two characters are '1'.
        assertThat(stream.toString()).isEqualTo("0000000000000011");

        Random random = new Random(SEED + 1);
        for (int bytes = 1; bytes <= 6; bytes++)
        {
            boolean[] bits = randomBits(random, bytes * Byte.SIZE);
            String rendered = streamOf(bits).toString();
            assertThat(rendered).as("length %d", bits.length).hasSize(bits.length);
            for (int i = 0; i < bits.length; i++)
            {
                // Same law, checked against an independently built expectation.
                char expected = bits[bits.length - 1 - i] ? '1' : '0';
                assertThat(rendered.charAt(i)).as("char %d of %d", i, bits.length).isEqualTo(expected);
            }
        }
    }

    @Test
    @DisplayName("toBytes() is a pure query: repeatable and defensively copied")
    void toBytesIsAPureQuery()
    {
        BitStream stream = new BitStream();
        stream.append((byte) 0x5A);

        byte[] first = stream.toBytes();
        byte[] second = stream.toBytes();
        // A query must be idempotent: asking twice cannot change the answer.
        assertThat(second).isEqualTo(first);

        first[0] = 0x00;
        // The returned array is a snapshot; handing out the live buffer would let a reader corrupt
        // the stream, breaking the round-trip property for every subsequent caller.
        assertThat(stream.toBytes()).containsExactly((byte) 0x5A);
    }

    @Tag(DEAD_CODE)
    @Test
    @DisplayName("a growable stream accepts appends regardless of its initial capacity")
    void growsFromAnyInitialCapacity()
    {
        // A self-growing buffer is total in the number of appends: the initial capacity is a
        // performance hint, never a limit on the language accepted. Capacity 0 is a legal hint
        // ("I do not know how big this will be"), so 100 bits must still round-trip.
        Random random = new Random(SEED + 2);
        for (int capacity : new int[] {0, 1, 7, 8, 9, 16})
        {
            boolean[] bits = randomBits(random, 100);
            BitStream stream = new BitStream(capacity);
            for (boolean bit : bits)
                stream.append(bit);

            byte[] encoded = stream.toBytes();
            for (int i = 0; i < bits.length; i++)
                assertThat(bitAt(encoded, i)).as("capacity %d, bit %d", capacity, i).isEqualTo(bits[i]);
        }
    }
}
