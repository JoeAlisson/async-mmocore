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

    char readChar();

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return int read
     */
    int readInt();

    float readFloat();

    long readLong();

    double readDouble();

    void readBytes(byte[] dst);

    void readBytes(byte[] dst, int offset, int length);

    int remaining();

    static ReadableBuffer of(ByteBuffer buffer) {
        return new SinglePacketBuffer(buffer);
    }
}
