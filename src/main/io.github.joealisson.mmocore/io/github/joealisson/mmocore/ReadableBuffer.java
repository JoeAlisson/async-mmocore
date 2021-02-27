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

import io.github.joealisson.mmocore.internal.SinglePacketBuffer;

import java.nio.ByteBuffer;

/**
 * @author JoeAlisson
 */
public interface ReadableBuffer extends Buffer {

    /**
     * Reads raw <B>byte</B> from the buffer
     * @return byte read
     */
    byte readByte();

    /**
     * Reads <B>short</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return short read
     */
    short readShort();

    /**
     * Reads <B>char</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return char read
     */
    char readChar();

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return int read
     */
    int readInt();

    /**
     * Reads <B>float</B> from the buffer. <BR>
     * 32bit float (00 00 00 00)
     * @return float read
     */
    float readFloat();

    /**
     * Reads <B>long</B> from the buffer. <BR>
     * 64bit integer (00 00 00 00 00 00 00 00)
     * @return long read
     */
    long readLong();

    /**
     * Reads <B>double</B> from the buffer. <BR>
     * 64bit float (00 00 00 00 00 00 00 00)
     * @return double read
     */
    double readDouble();

    /**
     *
     * Reads as many bytes as the length of the array.
     * @param dst : the byte array which will be filled with the data.
     */
    void readBytes(byte[] dst);

    /**
     *
     * Reads as many bytes as the given length (len). Starts to fill the
     * byte array from the given offset to <B>offset</B> + <B>len</B>.
     * @param dst : the byte array which will be filled with the data.
     * @param offset : starts to fill the byte array from the given offset.
     * @param length : the given length of bytes to be read.
     */
    void readBytes(byte[] dst, int offset, int length);

    /**
     * @return the available bytes amount to be read
     */
    int remaining();

    static ReadableBuffer of(ByteBuffer buffer) {
        return new SinglePacketBuffer(buffer);
    }
}
