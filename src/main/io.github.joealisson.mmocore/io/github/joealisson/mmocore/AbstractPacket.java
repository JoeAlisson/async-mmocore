package io.github.joealisson.mmocore;

import java.nio.ByteOrder;

public abstract class AbstractPacket<T> {

    static final boolean isBigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    byte[] data;
    int dataIndex;

    protected T client;

    /**
     *
     * @return The client that owners this packet
     */
    public T getClient() {
        return client;
    }

    static short convertEndian(short n) {
        return !isBigEndian ? n : Short.reverseBytes(n);
    }

    static int convertEndian(int n) {
        return !isBigEndian ? n : Integer.reverseBytes(n);
    }

    static long convertEndian(long n) {
        return !isBigEndian ? n : Long.reverseBytes(n);
    }

    static char convertEndian(char n) {
        return !isBigEndian ? n : Character.reverseBytes(n);
    }
}
