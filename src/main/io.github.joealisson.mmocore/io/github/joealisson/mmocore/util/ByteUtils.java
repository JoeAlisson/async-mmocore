package io.github.joealisson.mmocore.util;

import java.nio.ByteOrder;

public class ByteUtils {

    private static final boolean IS_NATIVE_BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private ByteUtils() {
        // utility class
    }

    public static short convertEndian(short n) {
        return IS_NATIVE_BIG_ENDIAN ? Short.reverseBytes(n) : n;
    }

    public static int convertEndian(int n) {
        return IS_NATIVE_BIG_ENDIAN ? Integer.reverseBytes(n) : n;
    }

    public static long convertEndian(long n) {
        return IS_NATIVE_BIG_ENDIAN ? Long.reverseBytes(n) : n;
    }

    public static char convertEndian(char n) {
        return IS_NATIVE_BIG_ENDIAN ? Character.reverseBytes(n) : n;
    }
}
