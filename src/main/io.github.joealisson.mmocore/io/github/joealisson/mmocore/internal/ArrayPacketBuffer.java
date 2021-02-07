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
import io.github.joealisson.mmocore.ResourcePool;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.lang.Byte.toUnsignedInt;
import static java.lang.Byte.toUnsignedLong;
import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Double.longBitsToDouble;
import static java.lang.Float.intBitsToFloat;
import static java.util.Objects.isNull;

/**
 * @author JoeAlisson
 */
public class ArrayPacketBuffer extends InternalWritableBuffer implements ReadableBuffer {

    private final ResourcePool resourcePool;
    private byte[] data;
    private int index;
    private int limit;

    public ArrayPacketBuffer(int size, ResourcePool resourcePool) {
        data = new byte[size];
        this.resourcePool = resourcePool;
    }

    @Override
    public void writeByte(byte value) {
        writeByte(index++, value);
    }

    @Override
    public void writeByte(int index, byte value) {
        ensureSize(index + 1);
        data[index] = value;
    }

    private void ensureSize(int size) {
        if(data.length < size) {
            data = Arrays.copyOf(data, (int) ((data.length + size) * 1.2));
            limit = data.length;
        }
    }

    @Override
    public void writeBytes(byte[] bytes) {
        if(isNull(bytes)) {
            return;
        }
        ensureSize(index + bytes.length);

        System.arraycopy(bytes, 0, data, index, bytes.length);
        index += bytes.length;
    }

    @Override
    public void writeShort(short value) {
        writeShort(index, value);
        index += 2;
    }

    @Override
    public void writeShort(int index, short value) {
        ensureSize(index + 2);
        data[index++] = (byte) value;
        data[index] = (byte) (value >>> 8);
    }

    @Override
    public void writeChar(char value) {
        writeShort((short) value);
    }


    @Override
    public void writeInt(int value) {
        writeInt(index, value);
        index += 4;
    }

    @Override
    public void writeInt(int index, int value) {
        ensureSize(index + 4);
        data[index++] = (byte) value;
        data[index++] = (byte) (value >>> 8);
        data[index++] = (byte) (value >>> 16);
        data[index] = (byte) (value >>> 24);
    }

    @Override
    public void writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
    }

    @Override
    public void writeLong(long value) {
        ensureSize(index + 8);
        data[index++] = (byte) value;
        data[index++] = (byte) (value >>> 8);
        data[index++] = (byte) (value >>> 16);
        data[index++] = (byte) (value >>> 24);
        data[index++] = (byte) (value >>> 32);
        data[index++] = (byte) (value >>> 40);
        data[index++] = (byte) (value >>> 48);
        data[index++] = (byte) (value >>> 56);
    }

    @Override
    public void writeDouble(double value) {
        writeLong(doubleToRawLongBits(value));
    }

    @Override
    public int position() {
        return index;
    }

    @Override
    public void position(int pos) {
        index = pos;
    }

    @Override
    public byte readByte() {
        return data[index++];
    }

    @Override
    public byte readByte(int index) {
        return data[index];
    }

    @Override
    public short readShort() {
        return (short) (readUnsigned(index++) | readUnsigned(index++) << 8);
    }

    @Override
    public short readShort(int index) {
        return (short) (readUnsigned(index++) | readUnsigned(index) << 8);
    }

    public char readChar() {
        return (char) readShort();
    }

    private int readUnsigned(int index) {
        return toUnsignedInt(data[index]);
    }

    @Override
    public int readInt() {
        return readUnsigned(index++)  |
                readUnsigned(index++) << 8  |
                readUnsigned(index++) << 16 |
                readUnsigned(index++) << 24;
    }

    @Override
    public float readFloat() {
        return intBitsToFloat(readInt());
    }

    @Override
    public long readLong() {
        return  toUnsignedLong(data[index++])  |
                toUnsignedLong(data[index++]) <<  8  |
                toUnsignedLong(data[index++]) << 16 |
                toUnsignedLong(data[index++]) << 24 |
                toUnsignedLong(data[index++]) << 32 |
                toUnsignedLong(data[index++]) << 40 |
                toUnsignedLong(data[index++]) << 48 |
                toUnsignedLong(data[index++]) << 56;
    }

    @Override
    public double readDouble() {
        return longBitsToDouble(readLong());
    }

    @Override
    public void readBytes(byte[] dst) {
        readBytes(dst, 0, dst.length);
    }

    @Override
    public void readBytes(byte[] dst, int offset, int length) {
        System.arraycopy(data, index, dst, offset, length);
        index += length;
    }

    @Override
    public int readInt(int index) {
        return readUnsigned(index++) | readUnsigned(index++) << 8 |  readUnsigned(index++) << 16 | readUnsigned(index) << 24;
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public void limit(int newLimit) {
        ensureSize(newLimit);
        limit = newLimit;
    }

    public void mark() {
        limit = index;
    }

    @Override
    public ByteBuffer[] toByteBuffers() {
        return new ByteBuffer[] { toByteBuffer() };
    }

    ByteBuffer toByteBuffer() {
        ByteBuffer buffer = resourcePool.getBuffer(limit);
        buffer.put(data, 0, limit);
        return  buffer.flip();
    }

    @Override
    public void releaseResources() {
        index = 0;
        limit = data.length;
    }

    @Override
    public int remaining() {
        return limit - index;
    }
}
