package io.github.joealisson.mmocore;

import java.nio.ByteOrder;

public abstract class AbstractPacket<T> {

    static final boolean IS_BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
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
        return !IS_BIG_ENDIAN ? n : Short.reverseBytes(n);
    }

    static int convertEndian(int n) {
        return !IS_BIG_ENDIAN ? n : Integer.reverseBytes(n);
    }

    static long convertEndian(long n) {
        return !IS_BIG_ENDIAN ? n : Long.reverseBytes(n);
    }

    static char convertEndian(char n) {
        return !IS_BIG_ENDIAN ? n : Character.reverseBytes(n);
    }
}
