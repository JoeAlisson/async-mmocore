package io.github.joealisson.mmocore;

import java.nio.charset.StandardCharsets;

import static java.lang.Byte.toUnsignedInt;
import static java.lang.Byte.toUnsignedLong;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Float.intBitsToFloat;

/**
 * This class represents a Packet received from the client.
 *
 * All data received must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 */
public abstract class ReadablePacket<T extends Client<Connection<T>>> implements Runnable {

    private PacketBuffer buffer;
    protected T client;

    protected ReadablePacket() {
        // no direct instances
    }

    void init(T client, PacketBuffer buffer) {
        this.client = client;
        this.buffer = buffer;
    }

    /**
     *
     * @return the available data to be read
     */
    protected final int available() {
        return buffer.data.length - buffer.index;
    }

    /**
     *
     * Reads as many bytes as the length of the array.
     * @param dst : the byte array which will be filled with the data.
     */
    protected final void readBytes(final byte[] dst) {
        readBytes(dst,0, dst.length);
    }

    /**
     *
     * Reads as many bytes as the given length (len). Starts to fill the
     * byte array from the given offset to <B>offset</B> + <B>len</B>.
     * @param dst : the byte array which will be filled with the data.
     * @param offset : starts to fill the byte array from the given offset.
     * @param length : the given length of bytes to be read.
     */
    protected final void readBytes(final byte[] dst, final int offset, final int length) {
        System.arraycopy(buffer.data, buffer.index, dst, offset, length);
        buffer.index += length;
    }

    /**
     * Reads raw <B>byte</B> from the buffer
     * @return byte read
     */
    protected final byte readByte() {
        return buffer.data[buffer.index++];
    }

    /**
     * Reads <B>byte</B> from the buffer
     * @return true if byte is equal 1
     */
    protected final boolean readByteAsBoolean() {
        return  readByte() == 1;
    }

    /**
     *  Reads <B>char</B> from the buffer
     * @return char read
     */
    protected final char readChar() {
        return (char) readShort();
    }

    /**
     * Reads <B>byte</B> from the buffer. <BR>
     * 8bit integer (00)
     * @return unsigned int read
     */
    protected final int readUnsignedByte() {
        return toUnsignedInt(buffer.data[buffer.index++]);
    }

    /**
     * Reads <B>short</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return short read
     */
    protected final short readShort()  {
        return (short) (readUnsignedByte() |
                        readUnsignedByte() << 8);
    }

    /**
     * Reads <B>short</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return  true if the short equals 1
     */
    protected final boolean readShortAsBoolean()  {
        return readShort() == 1;
    }

    /**
     * Reads <B>float</B> from the buffer. <BR>
     * 32bit precision float (00 00 00 00)
     * @return float read
     */
    protected final float readFloat() {
        return intBitsToFloat(readInt());
    }

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return int read
     */
    protected final int readInt() {
        return readUnsignedByte()  |
                readUnsignedByte() << 8  |
                readUnsignedByte() << 16 |
                readUnsignedByte() << 24 ;

    }

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return true if int equals 1
     */
    protected final boolean readIntAsBoolean() {
        return readInt() == 1;
    }

    /**
     * Reads <B>long</B> from the buffer. <BR>
     * 64bit integer (00 00 00 00 00 00 00 00)
     * @return long read
     */
    protected final long readLong() {
        return toUnsignedLong(readByte())  |
                toUnsignedLong(readByte()) <<  8  |
                toUnsignedLong(readByte()) << 16 |
                toUnsignedLong(readByte()) << 24 |
                toUnsignedLong(readByte()) << 32 |
                toUnsignedLong(readByte()) << 40 |
                toUnsignedLong(readByte()) << 48 |
                toUnsignedLong(readByte()) << 56;
    }

    /**
     * Reads <B>double</B> from the buffer. <BR>
     * 64bit double precision float (00 00 00 00 00 00 00 00)
     * @return double read
     */
    protected final double readDouble() {
        return longBitsToDouble(readLong());
    }

    /**
     * Reads <B>String</B> from the buffer.
     * @return String read
     */
    protected final String readString()  {
        StringBuilder builder = new StringBuilder();
        char c;
        while((c = readChar()) != '\000') {
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Reads a predefined length <B>String</B> from the buffer.
     * @return String read
     */
    protected final String readSizedString() {
        int size = readShort() * 2;
        byte[] data = new byte[size];
        readBytes(data);
        return new String(data, 0, size, StandardCharsets.UTF_16LE);
    }

    public T getClient() {
        return client;
    }

    protected abstract boolean read();
}