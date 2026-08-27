package io.github.turtleisaac.pokeditor.framework;

import java.util.Arrays;

public class BitVector {

    final long[] longs;

    /**
     * how many bits this vector was declared to hold. kept so that an out of range index can be
     * rejected: the backing array is rounded up to a whole number of longs, so without it the
     * slack bits at the top of the last long would silently accept indices past the end.
     */
    private final int maxBits;

    public BitVector(int maxBits) {
        if (maxBits < 0)
            throw new IllegalArgumentException("A bit vector cannot have a negative length: " + maxBits);
        this.maxBits = maxBits;
        // round up - otherwise new BitVector(100) would only allocate a single long
        int numLongs = (maxBits + Long.SIZE - 1) / Long.SIZE;
        longs = new long[numLongs];
    }

    /**
     * @return the number of bits this vector holds; valid indices are {@code 0..size()-1}
     */
    public int size() {
        return maxBits;
    }

    /**
     * Java truncates integer division toward zero and masks a shift count to its low six bits,
     * so an unchecked negative index does not fail - {@code idx = -1} lands on
     * {@code longs[0]} with {@code 1L << 63}, quietly flipping a real bit at the top of the
     * first word. Every index therefore gets checked before it is used.
     */
    private void checkIndex(int idx) {
        if (idx < 0 || idx >= maxBits)
            throw new IndexOutOfBoundsException("Bit index " + idx + " is outside 0.." + (maxBits - 1));
    }

    private static long mask(int idx) {
        return 1L << (idx % Long.SIZE);
    }

    public void setBit(int idx) {
        checkIndex(idx);
        longs[idx/Long.SIZE] |= mask(idx);
    }

    public void clearBit(int idx) {
        checkIndex(idx);
        longs[idx/Long.SIZE] &= ~mask(idx);
    }

    public boolean isSet(int idx) {
        checkIndex(idx);
        return (longs[idx/Long.SIZE] & mask(idx)) != 0;
    }

    public long[] toLongs() {
        return Arrays.copyOf(longs, longs.length);
    }
}
