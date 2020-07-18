package io.github.joealisson.mmocore.internal;

import io.github.joealisson.mmocore.Buffer;
import io.github.joealisson.mmocore.ResourcePool;

import java.nio.ByteBuffer;

/**
 * @author JoeAlisson
 */
public interface WritableBuffer extends Buffer {

    /**
     * Write a<B>byte</B> to the buffer. <BR>
     * 8bit integer (00)
     *
     * If the underlying data array can't hold a new byte its size is increased 20%
     *
     * @param value to be written
     */
    void writeByte(byte value);

    void writeBytes(byte[] value);

    /**
     * Write <B>short</B> to the buffer. <BR>
     * 16bit integer (00 00)
     * @param value to be written
     */
    void writeShort(short value);

    /**
     * Write <B>char</B> to the buffer.<BR>
     * 16 bit char
     *
     * @param value the char to be put on data.
     */
    void writeChar(char value);

    /**
     * Write <B>int</B> to the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @param value to be written
     */
    void writeInt(int value);

    /**
     * Write <B>float</B> to the buffer. <BR>
     *  32bit float point number (00 00 00 00)
     * @param value to be written
     */
    void writeFloat(float value);

    /**
     * Write <B>long</B> to the buffer. <BR>
     * 64bit integer (00 00 00 00 00 00 00 00)
     * @param value to be written
     */
    void writeLong(long value);

    /**
     * Write <B>double</B> to the buffer. <BR>
     * 64bit double precision float (00 00 00 00 00 00 00 00)
     * @param value to be written
     */
    void writeDouble(double value);

    int position();

    void position(int pos);

    void mark();

    ByteBuffer[] toByteBuffers();

    void releaseResources();

    static WritableBuffer dynamicOf(ByteBuffer buffer, ResourcePool resourcePool) {
        return new DynamicPacketBuffer(buffer, resourcePool);
    }

    static WritableBuffer of(int size, ResourcePool resourcePool) {
        return new ArrayPacketBuffer(size, resourcePool);
    }
}
