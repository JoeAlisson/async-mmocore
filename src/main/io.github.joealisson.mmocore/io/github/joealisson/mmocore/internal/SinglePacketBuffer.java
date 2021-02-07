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
package io.github.joealisson.mmocore.internal;

import io.github.joealisson.mmocore.ReadableBuffer;

import java.nio.ByteBuffer;

/**
 * @author JoeAlisson
 */
public class SinglePacketBuffer implements ReadableBuffer {

    private final ByteBuffer buffer;

    public SinglePacketBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public byte readByte() {
        return buffer.get();
    }

    @Override
    public short readShort() {
        return buffer.getShort();
    }

    @Override
    public char readChar() {
        return buffer.getChar();
    }

    @Override
    public int readInt() {
        return buffer.getInt();
    }

    @Override
    public float readFloat() {
        return buffer.getFloat();
    }

    @Override
    public long readLong() {
        return buffer.getLong();
    }

    @Override
    public double readDouble() {
        return buffer.getDouble();
    }

    @Override
    public void readBytes(byte[] dst) {
        buffer.get(dst);
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        buffer.get(dst, offset, length);
    }

    @Override
    public int remaining() {
        return buffer.remaining();
    }

    @Override
    public byte readByte(int index) {
        return buffer.get(index);
    }

    @Override
    public void writeByte(int index, byte value) {
        buffer.put(index, value);
    }

    @Override
    public short readShort(int index) {
        return buffer.getShort(index);
    }

    @Override
    public void writeShort(int index, short value) {
        buffer.putShort(index, value);
    }

    @Override
    public int limit() {
        return buffer.limit();
    }

    @Override
    public void limit(int newLimit) {
        buffer.limit(newLimit);
    }

    @Override
    public int readInt(int index) {
        return buffer.getInt(index);
    }

    @Override
    public void writeInt(int index, int value) {
        buffer.putInt(index, value);
    }

}
