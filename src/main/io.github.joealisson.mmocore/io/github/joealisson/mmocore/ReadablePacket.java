/*
 * Copyright © 2019-2021 Async-mmocore
 *
 * This file is part of the Async-mmocore project.
 *
 * Async-mmocore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Async-mmocore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.joealisson.mmocore;

import java.nio.charset.StandardCharsets;

import static java.lang.Byte.toUnsignedInt;

/**
 * This class represents a Packet received from the client.
 *
 * All data received must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 * @author JoeAlisson
 */
public abstract class ReadablePacket<T extends Client<Connection<T>>> implements Runnable {

    private ReadableBuffer buffer;
    protected T client;

    protected ReadablePacket() {
        // no direct instances
    }

    void init(T client, ReadableBuffer buffer) {
        this.client = client;
        this.buffer = buffer;
    }

    /**
     *
     * @return the available data to be read
     */
    protected final int available() {
        return buffer.remaining();
    }

    /**
     *
     * Reads as many bytes as the length of the array.
     * @param dst : the byte array which will be filled with the data.
     */
    protected final void readBytes(final byte[] dst) {
        buffer.readBytes(dst,0, dst.length);
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
        buffer.readBytes(dst, offset, length);
    }

    /**
     * Reads raw <B>byte</B> from the buffer
     * @return byte read
     */
    protected final byte readByte() {
        return buffer.readByte();
    }

    /**
     * Reads <B>byte</B> from the buffer
     * @return true if byte is not equal 0
     */
    protected final boolean readBoolean() {
        return readByte() != 0;
    }

    /**
     *  Reads <B>char</B> from the buffer
     * @return char read
     */
    protected final char readChar() {
        return buffer.readChar();
    }

    /**
     * Reads <B>byte</B> from the buffer. <BR>
     * 8bit integer (00)
     * @return unsigned int read
     */
    protected final int readUnsignedByte() {
        return toUnsignedInt(readByte());
    }

    /**
     * Reads <B>short</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return short read
     */
    protected final short readShort()  {
        return buffer.readShort();
    }

    /**
     * Reads <B>short</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return  true if the short is not equals 0
     */
    protected final boolean readShortAsBoolean()  {
        return readShort() != 0;
    }

    /**
     * Reads <B>float</B> from the buffer. <BR>
     * 32bit precision float (00 00 00 00)
     * @return float read
     */
    protected final float readFloat() {
        return buffer.readFloat();
    }

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return int read
     */
    protected final int readInt() {
        return buffer.readInt();

    }

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return true if int is not equals 0
     */
    protected final boolean readIntAsBoolean() {
        return readInt() != 0;
    }

    /**
     * Reads <B>long</B> from the buffer. <BR>
     * 64bit integer (00 00 00 00 00 00 00 00)
     * @return long read
     */
    protected final long readLong() {
        return buffer.readLong();
    }

    /**
     * Reads <B>double</B> from the buffer. <BR>
     * 64bit double precision float (00 00 00 00 00 00 00 00)
     * @return double read
     */
    protected final double readDouble() {
        return buffer.readDouble();
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