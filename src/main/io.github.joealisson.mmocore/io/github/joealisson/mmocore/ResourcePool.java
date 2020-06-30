package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
class ResourcePool {

    private final Map<Integer, Queue<ByteBuffer>> buffers = new HashMap<>(4);

    private final ConnectionConfig<?> config;

    private ResourcePool(ConnectionConfig<?> config) {
        this.config = config;
    }

    ByteBuffer getPooledDirectBuffer(int size) {
        return getSizedBuffer(determineBufferSize(size));
    }

    ByteBuffer getHeaderBuffer() {
        return getSizedBuffer(Client.HEADER_SIZE);
    }


    private ByteBuffer getSizedBuffer(int size) {
        Queue<ByteBuffer> queue = queueFromSize(size);
        ByteBuffer buffer = queue.poll();
        if(isNull(buffer)) {
            return ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer;
    }

    private Queue<ByteBuffer> queueFromSize(int size) {
        Queue<ByteBuffer> queue = buffers.get(size);
        if(isNull(queue)) {
            queue = new ConcurrentLinkedQueue<>();
            buffers.put(size, queue);
        }
        return queue;
    }

    private int determineBufferSize(int size) {
        int bufferSize = config.bufferDefaultSize;
        if(size <= config.bufferSmallSize) {
            bufferSize = config.bufferSmallSize;
        } else if( size <= config.bufferMediumSize) {
            bufferSize = config.bufferMediumSize;
        } else if( size <= config.bufferLargeSize) {
            bufferSize = config.bufferLargeSize;
        }
        return bufferSize;
    }

    void recycleBuffer(ByteBuffer buffer) {
        if (nonNull(buffer)) {
            Queue<ByteBuffer> queue;
            int poolSize =  determinePoolSize(buffer.capacity());
            queue = buffers.get(buffer.capacity());
            if (nonNull(queue) && queue.size() < poolSize) {
                buffer.clear();
                queue.add(buffer);
            }
        }
    }

    public ByteBuffer recycleAndGetNew(ByteBuffer buffer, int newSize) {
        if(nonNull(buffer)) {
            if(buffer.clear().limit() == determineBufferSize(newSize)) {
                return buffer;
            }
            recycleBuffer(buffer);
        }
        return getPooledDirectBuffer(newSize).limit(newSize);
    }

    private int determinePoolSize(int size) {
        int poolSize = config.bufferPoolSize;
        if(size == config.bufferSmallSize) {
            poolSize = config.bufferSmallPoolSize;
        } else if( size == config.bufferMediumSize) {
            poolSize = config.bufferMediumPoolSize;
        } else if( size == config.bufferLargePoolSize) {
            poolSize = config.bufferLargePoolSize;
        }
        return poolSize;
    }

    int getSmallSize() {
        return config.bufferSmallSize;
    }

    static ResourcePool initialize(ConnectionConfig<?> config) {
        return new ResourcePool(config);
    }
}