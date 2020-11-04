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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JoeAlisson
 */
public class BufferPool {

    private final Queue<ByteBuffer> buffers = new ConcurrentLinkedQueue<>();
    private final int maxSize;
    private final int bufferSize;
    private final AtomicInteger elements = new AtomicInteger();

    public BufferPool(int maxSize, int bufferSize) {
        this.maxSize = maxSize;
        this.bufferSize = bufferSize;
    }

    public void initialize(float factor) {
        final int amount = (int) Math.min(maxSize, maxSize * factor);
        for (int i = 0; i < amount; i++) {
            buffers.offer(ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.LITTLE_ENDIAN));
        }
        elements.set(amount);
    }

    public void recycle(ByteBuffer buffer) {
        if(elements.get() < maxSize) {
            buffers.offer(buffer.clear());
            elements.incrementAndGet();
        }
    }

    public ByteBuffer get() {
        if(elements.get() > 0) {
            elements.getAndDecrement();
            return buffers.poll();
        }
        return null;
    }
}
