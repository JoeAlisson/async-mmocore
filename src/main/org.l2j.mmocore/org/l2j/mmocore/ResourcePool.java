package org.l2j.mmocore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.nonNull;

class ResourcePool {

    private static ByteOrder byteOrder;
    private static int byteBufferPoolSize;
    static int bufferSize;

    private static final Queue<ByteBuffer> buffers = new ConcurrentLinkedQueue<>();

    static ByteBuffer getPooledBuffer() {
        ByteBuffer buffer = buffers.poll();
        return nonNull(buffer) ? buffer : ByteBuffer.allocateDirect(bufferSize).order(byteOrder);
    }

    static void recycleBuffer(ByteBuffer buffer) {
        if(nonNull(buffer) && buffers.size() < byteBufferPoolSize) {
            buffer.clear();
            buffers.add(buffer);
        }
    }

    static void setBufferSize(int size) {
        bufferSize = size;
    }
    static void setBufferPoolSize(int size) {
        byteBufferPoolSize = size;
    }
    static void setByteOrder(ByteOrder order) {
        byteOrder = order;
    }
}