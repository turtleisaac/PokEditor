package io.github.turtleisaac.pokeditor.framework;

import java.util.Arrays;

public class BitStream {

    private byte[] bytes;
    private int nextBit;

    private static final int DEFAULT_CAPACITY = 1024*Byte.SIZE;
    private static final String STRING_ZEROS = "0000000";

    public BitStream() {
        this(DEFAULT_CAPACITY);
    }

    public BitStream(int initialCapacity) {
        nextBit = 0;
        if (initialCapacity%Byte.SIZE != 0) {
            ++initialCapacity;
        }
        bytes = new byte[initialCapacity];
    }

    public void append(boolean value) {
        ensureSize();
        if (value) {
            bytes[nextBit / Byte.SIZE] |= 1 << (nextBit % Byte.SIZE);
        }
        ++nextBit;
    }

    public void append(byte b) {
        for (int i=0; i<Byte.SIZE; i++) {
            ensureSize();
            if ((b & (1<<i)) != 0) {
                bytes[nextBit / Byte.SIZE] |= 1 << (nextBit % Byte.SIZE);
            }
            ++nextBit;
        }
    }

    public void append(boolean value, int count) {
        for (int i=0; i<count; i++) {
            append(value);
        }
    }

    public byte[] toBytes() {
        int size = nextBit/Byte.SIZE + (nextBit%Byte.SIZE == 0 ? 0 : 1);
        return Arrays.copyOf(bytes, size);
    }

    private void ensureSize() {
        if (nextBit/Byte.SIZE == bytes.length) {
            bytes = Arrays.copyOf(bytes, bytes.length*2);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : toBytes()) {
            sb.insert(0, fixBinaryString(Integer.toBinaryString(b&0xff)));
        }
        return sb.toString();
    }

    private String fixBinaryString(String str) {
        return STRING_ZEROS.substring(0, 8-str.length()) + str;
    }
}
