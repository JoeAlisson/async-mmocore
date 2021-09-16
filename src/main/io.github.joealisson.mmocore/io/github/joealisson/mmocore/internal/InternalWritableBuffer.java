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

import io.github.joealisson.mmocore.ResourcePool;
import io.github.joealisson.mmocore.WritableBuffer;

import java.nio.ByteBuffer;

/**
 * @author JoeAlisson
 */
public abstract class InternalWritableBuffer extends WritableBuffer {

    /**
     * @return the current buffer position
     */
    public abstract int position();

    /**
     * set the buffer position
     *
     * @param pos the new buffer position
     */
    public abstract void position(int pos);

    /**
     * mark the end of buffer's content
     */
    public abstract void mark();

    /**
     * transform the Writable Buffer into a array of ByteBuffers
     *
     * @return an array of ByteBuffers with WritableBuffers' content
     */
    public abstract ByteBuffer[] toByteBuffers();

    /**
     * release the resources used
     */
    public abstract void releaseResources();

    /**
     * Create a new Dynamic Buffer that increases as needed
     *
     * @param buffer the initial under layer buffer
     * @param resourcePool the resource pool used to get new buffers when needed
     * @return a new Dynamic Buffer
     */
    public static InternalWritableBuffer dynamicOf(ByteBuffer buffer, ResourcePool resourcePool) {
        return new DynamicPacketBuffer(buffer, resourcePool);
    }

    /**
     * Create a new Dynamic Buffer that increases as needed based on ArrayPacketBuffer
     *
     * @param buffer the base buffer
     * @param resourcePool the resource pool used to get new buffers when needed
     * @return a new Dynamic buffer
     */
    public static InternalWritableBuffer dynamicOf(ArrayPacketBuffer buffer, ResourcePool resourcePool) {
        var copy = new DynamicPacketBuffer(buffer.toByteBuffer(), resourcePool);
        copy.limit(buffer.limit());
        return copy;
    }

    /**
     * Create a new buffer backed by array
     * @param resourcePool the resource pool used to get new buffers
     * @return a Buffer backed by array
     */
    public static InternalWritableBuffer arrayBacked(ResourcePool resourcePool) {
        return new ArrayPacketBuffer(resourcePool.getSegmentSize(), resourcePool);
    }
}
