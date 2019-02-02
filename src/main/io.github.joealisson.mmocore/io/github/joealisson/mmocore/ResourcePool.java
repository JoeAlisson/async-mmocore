package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

class ResourcePool {

    private final Map<Integer, Queue<ByteBuffer>> directBuffers = new HashMap<>();
    private final Map<Integer, Queue<ByteBuffer>> buffers = new HashMap<>();
    private final ConnectionConfig config;

    private ResourcePool(ConnectionConfig config) {
        this.config = config;
    }

    ByteBuffer getPooledDirectBuffer() {
        return getSizedBuffer(config.bufferDefaultSize, true);
    }

    ByteBuffer getPooledBuffer() {
        return getSizedBuffer(config.bufferDefaultSize, false);
    }

    ByteBuffer getPooledDirectBuffer(int size) {
        return getSizedBuffer(determineBufferSize(size), true);
    }

    ByteBuffer getPooledBuffer(int size) {
        return getSizedBuffer(determineBufferSize(size), false);
    }

    private ByteBuffer getSizedBuffer(int size, boolean direct) {
        Queue<ByteBuffer> queue = queueFromSize(size, direct);
        ByteBuffer buffer = queue.poll();
        if(isNull(buffer)) {
            return direct ? ByteBuffer.allocateDirect(size).order(config.byteOrder) : ByteBuffer.allocate(size).order(config.byteOrder);
        }
        return buffer;
    }

    private Queue<ByteBuffer> queueFromSize(int size, boolean direct) {
        Queue<ByteBuffer> queue = direct ? directBuffers.get(size) : buffers.get(size);
        if(isNull(queue)) {
            queue = new ConcurrentLinkedQueue<>();
            if(direct) {
                directBuffers.put(size, queue);
            } else {
                buffers.put(size, queue);
            }
        }
        return queue;
    }

    private int determineBufferSize(int size) {
        int newSize = config.bufferDefaultSize;
        if(size <= config.bufferMinSize) {
            newSize = config.bufferMinSize;
        } else if( size <= config.bufferMediumSize) {
            newSize = config.bufferMediumSize;
        } else if( size <= config.bufferLargeSize) {
            newSize = config.bufferLargeSize;
        }
        return newSize;
    }

    void recycleBuffer(ByteBuffer buffer) {
        if (nonNull(buffer)) {
            Queue<ByteBuffer> queue;
            int poolSize =  determinePoolSize(buffer.capacity());
            if(buffer.isDirect()) {
                queue = directBuffers.get(buffer.capacity());
            } else {
                queue = buffers.get(buffer.capacity());
            }
            if (nonNull(queue) && queue.size() < poolSize) {
                buffer.clear();
                queue.add(buffer);
            }
        }
    }

    private int determinePoolSize(int size) {
        int poolSize = config.bufferPoolSize;
        if(size == config.bufferMinSize) {
            poolSize = config.bufferMinPoolSize;
        } else if( size == config.bufferMediumSize) {
            poolSize = config.bufferMediumPoolSize;
        } else if( size == config.bufferLargePoolSize) {
            poolSize = config.bufferLargePoolSize;
        }
        return poolSize;
    }

    static ResourcePool initialize(ConnectionConfig config) {
        return new ResourcePool(config);
    }
}