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

    public static InternalWritableBuffer of(int size, ResourcePool resourcePool) {
        return new ArrayPacketBuffer(size, resourcePool);
    }
}
