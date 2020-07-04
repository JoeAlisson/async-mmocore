/*
 * Copyright © 2019-2020 Async-mmocore
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

import static java.lang.Byte.toUnsignedInt;

/**
 * @author JoeAlisson
 */
public class PacketBuffer {
    byte[] data;
    int index = ConnectionConfig.HEADER_SIZE;

    private PacketBuffer() {
        // no direct instances
    }

    static PacketBuffer of(int size) {
        PacketBuffer buffer = new PacketBuffer();
        buffer.data = new byte[size];
        return buffer;
    }

    static PacketBuffer of(final byte[] data, int dataIndex) {
        PacketBuffer buffer = new PacketBuffer();
        buffer.data = data;
        buffer.index = dataIndex;
        return buffer;
    }

    /**
     * Reads raw <B>byte</B> from the buffer
     * @return byte read
     */
    public byte read() {
        return data[index++];
    }

    /**
     * Reads <B>short</B> from the buffer. <BR>
     * 16bit integer (00 00)
     * @return short read
     */
    public short readShort()  {
        return (short) (readUnsigned() | readUnsigned() << 8);
    }

    private int readUnsigned() {
        return toUnsignedInt(read());
    }

    /**
     * Reads <B>int</B> from the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @return int read
     */
    public final int readInt() {
        return readUnsigned()  |
                            readUnsigned() << 8  |
                            readUnsigned() << 16 |
                            readUnsigned() << 24;

    }

    public byte[] expose() {
        return data;
    }

    public int remaining() {
        return data.length - index;
    }
}