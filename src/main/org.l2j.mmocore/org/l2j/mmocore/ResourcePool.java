package org.l2j.mmocore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.nonNull;

class ResourcePool {

    private static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    private static final int BUFFER_SIZE = 9 * 1024;
    private static final int BYTE_BUFFER_POOL_SIZE = 100;

    private static final Queue<ByteBuffer> buffers = new ConcurrentLinkedQueue<>();

    static ByteBuffer getPooledBuffer() {
        ByteBuffer buffer = buffers.poll();
        return nonNull(buffer) ? buffer : ByteBuffer.allocateDirect(BUFFER_SIZE).order(BYTE_ORDER);
    }

    static void recycleBuffer(ByteBuffer buffer) {
        if(nonNull(buffer) && buffers.size() < BYTE_BUFFER_POOL_SIZE) {
            buffer.clear();
            buffers.add(buffer);
        }
    }
}
