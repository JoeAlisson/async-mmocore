package io.github.joealisson.mmocore;

import java.nio.ByteOrder;

public abstract class AbstractPacket<T extends Client<Connection<T>>> {

    static final boolean IS_NATIVE_BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
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

    short convertEndian(short n) {
        return isNativeEndian() ? n : Short.reverseBytes(n);
    }

    int convertEndian(int n) {
        return isNativeEndian() ? n : Integer.reverseBytes(n);
    }

    long convertEndian(long n) {
        return isNativeEndian() ? n : Long.reverseBytes(n);
    }

    char convertEndian(char n) {
        return  isNativeEndian() ? n : Character.reverseBytes(n);
    }

    boolean isNativeEndian() {
        return client.getResourcePool().isNativeOrder();
    }
}
