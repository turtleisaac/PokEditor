package io.github.turtleisaac.pokeditor.framework;

import java.util.Arrays;

public class BitVector {

    final long[] longs;

    public BitVector(int maxBits) {
        // round up - otherwise new BitVector(100) would only allocate a single long
        int numLongs = (maxBits + Long.SIZE - 1) / Long.SIZE;
        longs = new long[numLongs];
    }

    private static long mask(int idx) {
        return 1L << (idx % Long.SIZE);
    }

    public void setBit(int idx) {
        longs[idx/Long.SIZE] |= mask(idx);
    }

    public void clearBit(int idx) {
        longs[idx/Long.SIZE] &= ~mask(idx);
    }

    public boolean isSet(int idx) {
        return (longs[idx/Long.SIZE] & mask(idx)) != 0;
    }

    public long[] toLongs() {
        return Arrays.copyOf(longs, longs.length);
    }
}
