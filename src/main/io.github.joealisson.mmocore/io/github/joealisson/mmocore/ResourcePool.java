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

import io.github.joealisson.mmocore.internal.BufferPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
class ResourcePool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcePool.class);

    private final ConnectionConfig<?> config;
    private final int[] bufferSizes;

    private ResourcePool(ConnectionConfig<?> config) {
        this.config = config;
        bufferSizes = config.bufferPools.keySet().stream().sorted().mapToInt(Integer::intValue).toArray();
    }

    ByteBuffer getHeaderBuffer() {
        return getSizedBuffer(ConnectionConfig.HEADER_SIZE);
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
        BufferPool pool = config.bufferPools.get(size);
        ByteBuffer buffer;
        if(isNull(pool) || isNull(buffer = pool.get()) ) {
            buffer = ByteBuffer.allocateDirect(size).order(ByteOrder.LITTLE_ENDIAN);
        }
        return buffer;
    }

    private int determineBufferSize(int size) {
        for (int bufferSize : bufferSizes) {
            if(size < bufferSize) {
                return bufferSize;
            }
        }
        LOGGER.warn("There is no buffer pool handling buffer size {}", size);
        return size;
    }

    void recycleBuffer(ByteBuffer buffer) {
        if (nonNull(buffer)) {
            BufferPool pool = config.bufferPools.get(buffer.capacity());
            if(nonNull(pool)) {
                pool.recycle(buffer);
            }
        }
    }

    int medianSize() {
        return bufferSizes[(int) Math.ceil(bufferSizes.length >> 1)];
    }

    static ResourcePool initialize(ConnectionConfig<?> config) {
        return new ResourcePool(config);
    }
}