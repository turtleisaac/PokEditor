package io.github.turtleisaac.pokeditor.framework;

import java.util.Arrays;

public class BitVector {

    final long[] longs;

    public BitVector(int maxBits) {
        int numLongs = maxBits / Long.SIZE;
        longs = new long[numLongs];
    }

    public void setBit(int idx) {
        longs[idx/Long.SIZE] |= idx << (idx%Long.SIZE);
    }

    public void clearBit(int idx) {
        longs[idx/Long.SIZE] &= ~(idx << (idx%Long.SIZE));
    }

    public boolean isSet(int idx) {
        return (longs[idx/Long.SIZE] & (idx << (idx%Long.SIZE))) != 0;
    }

    public long[] toLongs() {
        return Arrays.copyOf(longs, longs.length);
    }
}
