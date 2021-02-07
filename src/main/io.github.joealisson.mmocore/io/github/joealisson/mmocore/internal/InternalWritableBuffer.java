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

    public abstract int position();

    public abstract void position(int pos);

    public abstract void mark();

    public abstract ByteBuffer[] toByteBuffers();

    public abstract void releaseResources();

    public static InternalWritableBuffer dynamicOf(ByteBuffer buffer, ResourcePool resourcePool) {
        return new DynamicPacketBuffer(buffer, resourcePool);
    }

    public static InternalWritableBuffer dynamicOf(ArrayPacketBuffer buffer, ResourcePool resourcePool) {
        var copy = new DynamicPacketBuffer(buffer.toByteBuffer(), resourcePool);
        copy.limit(buffer.limit());
        return copy;
    }

    public static InternalWritableBuffer arrayBacked(ResourcePool resourcePool) {
        return new ArrayPacketBuffer(resourcePool.getSegmentSize(), resourcePool);
    }
}
