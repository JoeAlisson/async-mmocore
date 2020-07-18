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
package io.github.joealisson.mmocore.internal;

import io.github.joealisson.mmocore.ResourcePool;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.lang.Math.min;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public class DynamicPacketBuffer implements WritableBuffer {

    private PacketNode[] nodes = new PacketNode[8];
    private PacketNode currentNode;
    private final ResourcePool resourcePool;

    private int nodeCount;
    private int bufferIndex;
    private int limit;

    public DynamicPacketBuffer(ByteBuffer buffer, ResourcePool resourcePool) {
        this.resourcePool = resourcePool;
        newNode(buffer, 0);
    }

    private void newNode(ByteBuffer buffer, int initialIndex) {
        if(nodes.length == nodeCount) {
            nodes = Arrays.copyOf(nodes, (int) ((nodes.length + 1) * 1.2));
        }
        PacketNode node = new PacketNode(buffer, initialIndex, nodeCount);
        nodes[nodeCount++] = node;
        limit = node.endIndex;
    }

    @Override
    public void writeByte(byte value) {
        ensureSize(bufferIndex + 1);
        setByte(bufferIndex++, value);
    }

    @Override
    public void writeByte(int index, byte value) {
        checkBounds(index, 1);
        setByte(index, value);
    }

    private void checkBounds(int index, int length) {
        if(index < 0 || index + length > limit) {
            throw new IndexOutOfBoundsException("Trying access index " + index + " until index " + (index + length) +" , max accessible index is " + limit);
        }
    }

    private void setByte(int index, byte value) {
        PacketNode node = indexToNode(index);
        node.buffer.put(node.idx(index), value);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        ensureSize(bufferIndex + bytes.length);
        setBytes(bufferIndex, bytes);
        bufferIndex += bytes.length;
    }

    private void setBytes(int index, byte[] bytes) {
        PacketNode node = indexToNode(index);
        int length = bytes.length;
        int offset = 0;
        do {
            int available = min(length, node.endIndex - index);
            node.buffer.position(node.idx(index));
            node.buffer.put(bytes, offset, available);
            node.buffer.position(0);
            length -= available;
            offset += available;
            index += available;
            node = nodes[min(node.offset + 1, nodes.length - 1)];
        } while (length > 0);
    }

    @Override
    public void writeShort(short value) {
        ensureSize(bufferIndex + 2);
        setShort(bufferIndex, value);
        bufferIndex += 2;
    }

    @Override
    public void writeShort(int index, short value) {
        checkBounds(index, 2);
        setShort(index, value);
    }

    private void setShort(int index, short value) {
        PacketNode node = indexToNode(index);
        if(index + 2 <= node.endIndex) {
            node.buffer.putShort(node.idx(index), value);
        } else {
            setByte(index, (byte) value);
            setByte(index + 1, (byte) (value >>> 8));
        }
    }

    @Override
    public void writeChar(char value) {
        writeShort((short) value);
    }

    @Override
    public void writeInt(int value) {
        ensureSize(bufferIndex + 4);
        setInt(bufferIndex, value);
        bufferIndex += 4;
    }

    @Override
    public void writeInt(int index, int value) {
        checkBounds(index, 4);
        setInt(index, value);
    }

    private void setInt(int index, int value) {
        PacketNode node = indexToNode(index);
        if(index + 4 <= node.endIndex) {
            node.buffer.putInt(node.idx(index), value);
        } else {
            setShort(index, (short) value);
            setShort(index + 2, (short) (value >>> 16));
        }
    }

    @Override
    public void writeFloat(float value) {
        writeInt(Float.floatToRawIntBits(value));
    }

    @Override
    public void writeLong(long value) {
        ensureSize(bufferIndex + 8);
        setLong(bufferIndex, value);
        bufferIndex+= 8;
    }

    private void setLong(int index, long value) {
        PacketNode node = indexToNode(index);
        if(index + 8 <= node.endIndex) {
            node.buffer.putLong(node.idx(index), value);
        } else {
            setInt(index, (int) value);
            setInt(index + 4, (int) (value >>> 32));
        }
    }

    @Override
    public void writeDouble(double value) {
        writeLong(Double.doubleToRawLongBits(value));
    }

    @Override
    public int position() {
        return bufferIndex;
    }

    @Override
    public void position(int pos) {
        bufferIndex = pos;
    }

    @Override
    public byte readByte(int index) {
        checkSize(index + 1);
        return getByte(index);
    }

    private byte getByte(int index) {
        PacketNode node = indexToNode(index);
        return node.buffer.get(node.idx(index));
    }

    @Override
    public short readShort(int index) {
        checkSize(index + 2);
        return getShort(index);
    }

    private short getShort(int index) {
        PacketNode node = indexToNode(index);
        if(index + 2 <= node.endIndex) {
            return node.buffer.getShort(node.idx(index));
        } else {
            return (short) (getByte(index) & 0xFF | (getByte(index + 1) & 0xFF) << 8);
        }
    }

    @Override
    public int readInt(int index) {
        checkSize(index + 4);
        PacketNode node = indexToNode(index);
        if(index + 4 <= node.endIndex) {
            return node.buffer.getInt(node.idx(index));
        } else {
            return getShort(index) & 0xFFFF | (getShort(index + 2) & 0xFFFF) << 16;
        }
    }

    @Override
    public int limit() {
        return limit;
    }

    @Override
    public void limit(int newLimit) {
        if(limit != capacity()) {
            PacketNode node = indexToNode(limit);
            node.buffer.clear();
        }
        ensureSize(newLimit + 1);
        limit = newLimit;
        limitBuffer();
    }

    public int capacity() {
        return nodes[nodeCount - 1].endIndex;
    }

    public void mark() {
        limit = bufferIndex;
        limitBuffer();
    }

    private void limitBuffer() {
        PacketNode node = indexToNode(limit);
        node.buffer.limit(node.idx(limit));
    }

    private void ensureSize(int sizeRequired) {
        if(capacity() < sizeRequired) {
            int newSize = 64;
            while (newSize < sizeRequired) {
                newSize <<= 1;
            }
            increaseBuffers(newSize);
        }
    }

    private void increaseBuffers(int size) {
        int diffSize = size - capacity();
        ByteBuffer buffer = resourcePool.getBuffer(diffSize);
        PacketNode lastNode = nodes[nodeCount - 1];
        newNode(buffer, lastNode.endIndex);
    }

    private void checkSize(int size) {
        if(limit < size || size < 0) {
            throw new IndexOutOfBoundsException("Trying access index " + size + ", max size is " + limit);
        }
    }

    private PacketNode indexToNode(int index) {
        if(nonNull(currentNode) && currentNode.initialIndex <= index && currentNode.endIndex > index) {
            return currentNode;
        }

        int min = 0;
        int max = nodeCount;
        while (min <= max) {
            int mid = (min + max) >>> 1;
            PacketNode node = nodes[mid];
            if(index >= node.endIndex) {
                min = mid + 1;
            } else if (index < node.initialIndex) {
                max = mid - 1;
            } else {
                currentNode = node;
                return node;
            }
        }
        throw new IndexOutOfBoundsException("Could not map the index to a node: " + index);
    }

    @Override
    public ByteBuffer[] toByteBuffers() {
        int maxNode = indexToNode(limit).offset;
        ByteBuffer[] buffers = new ByteBuffer[maxNode+1];
        for (int i = 0; i <= maxNode; i++) {
            buffers[i] = nodes[i].buffer;
        }
        return buffers;
    }

    @Override
    public void releaseResources() {
        for (int i = 0; i < nodeCount; i++) {
            resourcePool.recycleBuffer(nodes[i].buffer);
            nodes[i] = null;
        }
        nodeCount = 0;
        bufferIndex = 0;
    }

    private static class PacketNode {
        private final ByteBuffer buffer;
        private final int initialIndex;
        private final int endIndex;
        private final int offset;

        private PacketNode(ByteBuffer buffer, int initialIndex, int offset) {
            this.buffer = buffer;
            this.initialIndex = initialIndex;
            this.endIndex = initialIndex + buffer.capacity();
            this.offset = offset;
        }

        public int idx(int index) {
            return index - initialIndex;
        }
    }
}
