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

import io.github.joealisson.mmocore.internal.BufferPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public class ResourcePool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcePool.class);

    private final Map<Integer, BufferPool> bufferPools;
    private int[] bufferSizes;
    private int bufferSegmentSize;

    ResourcePool() {
        bufferSizes = new int[] { 2, 64 };
        bufferPools = new HashMap<>(4);
        bufferSegmentSize = 64;
    }

    ByteBuffer getHeaderBuffer() {
        return getSizedBuffer(ConnectionConfig.HEADER_SIZE);
    }

    ByteBuffer getSegmentBuffer() {
        return getSizedBuffer(bufferSegmentSize);
    }

    public ByteBuffer getBuffer(int size) {
        return getSizedBuffer(determineBufferSize(size));
    }

    ByteBuffer recycleAndGetNew(ByteBuffer buffer, int newSize) {
        int bufferSize = determineBufferSize(newSize);
        if(nonNull(buffer)) {
            if(buffer.clear().limit() == bufferSize) {
                return buffer.limit(newSize);
            }
            recycleBuffer(buffer);
        }
        return getSizedBuffer(bufferSize).limit(newSize);
    }

    private ByteBuffer getSizedBuffer(int size) {
        BufferPool pool = bufferPools.get(size);
        ByteBuffer buffer = null;
        if(nonNull(pool)) {
            buffer = pool.get();
        }
        if(isNull(buffer)) {
            buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer;
    }

    private int determineBufferSize(int size) {
        for (int bufferSize : bufferSizes) {
            if(size <= bufferSize) {
                return bufferSize;
            }
        }
        LOGGER.warn("There is no buffer pool handling buffer size {}", size);
        return size;
    }

    public void recycleBuffer(ByteBuffer buffer) {
        if (nonNull(buffer)) {
            BufferPool pool = bufferPools.get(buffer.capacity());
            if(isNull(pool) || !pool.recycle(buffer)) {
                LOGGER.debug("buffer was not recycled {} in pool {}", buffer, pool);
            }
        }
    }

    public int getSegmentSize() {
        return bufferSegmentSize;
    }

    void addBufferPool(int bufferSize, BufferPool bufferPool) {
        bufferPools.putIfAbsent(bufferSize, bufferPool);
    }

    int bufferPoolSize() {
        return bufferPools.size();
    }

    void initializeBuffers(float initBufferPoolFactor) {
        if(initBufferPoolFactor > 0) {
            bufferPools.values().forEach(pool -> pool.initialize(initBufferPoolFactor));
        }
        bufferSizes = bufferPools.keySet().stream().sorted().mapToInt(Integer::intValue).toArray();
    }

    void setBufferSegmentSize(int size) {
        bufferSegmentSize = size;
    }

    String stats() {
        var sb = new StringBuilder();
        for (BufferPool pool : bufferPools.values()) {
            sb.append(pool.toString()).append("\n");
        }
        return sb.toString();
    }

}