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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Double.doubleToRawLongBits;
import static java.lang.Math.max;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * This class represents a Packet that can be sent to clients.
 *
 * All data sent must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 * @author JoeAlisson
 */
public abstract class WritablePacket<T extends Client<Connection<T>>> {

    private static final Map<Class<?>, Integer> packetInfo = new ConcurrentHashMap<>();
    private static final ThreadLocal<PacketBuffer> THREAD_LOCAL = new ThreadLocal<>();

    private byte[] staticData;


    protected WritablePacket() { }


    /**
     * Write <B>byte[]</B> to the buffer. <BR>
     * 8bit integer array (00 ...)
     * @param bytes to be written
     */
    protected final void writeBytes(final byte... bytes) {
        if(isNull(bytes)) {
            return;
        }
        PacketBuffer buffer  = THREAD_LOCAL.get();
        ensureSize(buffer, bytes.length);

        System.arraycopy(bytes, 0, buffer.data, buffer.index, bytes.length);
        buffer.index += bytes.length;
    }

    private void ensureSize(PacketBuffer buffer, int size) {
        if(buffer.data.length < buffer.index + size) {
            buffer.data = Arrays.copyOf(buffer.data, (int) ((buffer.data.length + size) * 1.2));
        }
    }

    /**
     * Write a<B>byte</B> to the buffer. <BR>
     * 8bit integer (00)
     *
     * If the underlying data array can't hold a new byte its size is increased 20%
     *
     * @param value to be written
     */
    protected final void writeByte(final byte value) {
        PacketBuffer buffer = THREAD_LOCAL.get();
        ensureSize(buffer, 1);
        buffer.data[buffer.index++] = value;
    }

    /**
     * Write a int to the buffer, the int is casted to a byte;
     *
     * @param value to be written
     */
    protected final void writeByte(final int value) {
        writeByte((byte) value);
    }

    /**
     * Write <B>boolean</B> to the buffer. <BR>
     *  If the value is true so write a <B>byte</B> with value 1, otherwise 0
     *  8bit integer (00)
     * @param value to be written
     */
    protected final void writeByte(final boolean value) {
        writeByte((byte) (value ? 0x01 : 0x00));
    }

    /**
     * Write <B>double</B> to the buffer. <BR>
     * 64bit double precision float (00 00 00 00 00 00 00 00)
     * @param value to be written
     */
    protected final void writeDouble(final double value) {
        long x = doubleToRawLongBits(value);
        writeLong(x);
    }

    /**
     * Write <B>short</B> to the buffer. <BR>
     * 16bit integer (00 00)
     * @param value to be written
     */
    protected final void writeShort(final int value) {
        writeBytes((byte)  value,
                   (byte) (value >>> 8));
    }

    /**
     * Write <B>boolean</B> to the buffer. <BR>
     * If the value is true so write a <B>byte</B> with value 1, otherwise 0
     *  16bit integer (00 00)
     * @param value to be written
     */
    protected final void writeShort(final boolean value) {
        writeShort(value ? 0x01 : 0x00);
    }

    /**
     * Write <B>int</B> to the buffer. <BR>
     * 32bit integer (00 00 00 00)
     * @param value to be written
     */
    protected final void writeInt(final int value) {
        writeBytes((byte)  value,
                   (byte) (value >>> 8),
                   (byte) (value >>> 16),
                   (byte) (value >>> 24));
    }

    /**
     * Write <B>boolean</B> to the buffer. <BR>
     * If the value is true so write a <B>byte</B> with value 1, otherwise 0
     *  32bit integer (00 00 00 00)
     * @param value to be written
     */
    protected final void writeInt(final boolean value) {
        writeInt(value ? 0x01 : 0x00);
    }


    /**
     * Write <B>float</B> to the buffer. <BR>
     *  32bit float point number (00 00 00 00)
     * @param value to be written
     */
    protected final void writeFloat(final float value) {
        int x  = Float.floatToRawIntBits(value);
        writeInt(x);
    }

    /**
     * Write <B>long</B> to the buffer. <BR>
     * 64bit integer (00 00 00 00 00 00 00 00)
     * @param value to be written
     */
    protected final void writeLong(final long value) {
        writeBytes((byte) value,
                (byte) (value >>> 8),
                (byte) (value >>> 16),
                (byte) (value >>> 24),
                (byte) (value >>> 32),
                (byte) (value >>> 40),
                (byte) (value >>> 48),
                (byte) (value >>> 56));
    }

    /**
     * Write <B>char</B> to the buffer.<BR>
     * 16 bit char
     *
     * @param value the char to be put on data.
     */
    protected  final void writeChar(final char value) {
        writeBytes((byte) value, (byte) (value >>> 8));
    }

    /**
     * Write a <B>String</B> to the buffer with a null termination (\000).
     * Each character is a 16bit char
     *
     * @param text to be written
     */
    protected final void writeString(final CharSequence text) {
        if(isNull(text)) {
            writeChar('\000');
            return;
        }
        writeStringWithCharset(text);
        writeChar('\000');
    }

    private void writeStringWithCharset(CharSequence text) {
        writeBytes(text.toString().getBytes(StandardCharsets.UTF_16LE));
    }

    /**
     * Write <B>String</B> to the buffer preceded by a <B>short</B> 16 bit with String length and no null termination.
     * Each character is a 16bit char.
     *
     * @param text to be written
     */
    protected final void writeSizedString(final CharSequence text) {
        if(nonNull(text) && text.length() > 0) {
            writeShort(text.length());
            writeStringWithCharset(text);
        } else {
            writeShort(0);
        }
    }

    int writeData(T client) {
        if(hasWritedStaticData()) {
            THREAD_LOCAL.set(PacketBuffer.of(Arrays.copyOf(staticData, staticData.length), staticData.length));
            return staticData.length;
        }

        THREAD_LOCAL.set(PacketBuffer.of( packetInfo.getOrDefault(getClass(), client.getResourcePool().medianSize())));

        if(write(client)) {
            PacketBuffer buffer = THREAD_LOCAL.get();
            if(getClass().isAnnotationPresent(StaticPacket.class)) {
                staticData = Arrays.copyOf(buffer.data, buffer.index);
            }
            return buffer.index;
        }
        return 0;
    }

    void writeHeaderAndRecord(int header) {
        PacketBuffer buffer = THREAD_LOCAL.get();
        short size =  (short) header;
        buffer.data[0] = (byte) size;
        buffer.data[1] = (byte) (size >>> 8);

        packetInfo.compute(getClass(), (k, v) -> isNull(v) ? header : max(v, header));
    }

    private boolean hasWritedStaticData() {
        return getClass().isAnnotationPresent(StaticPacket.class) && nonNull(staticData);
    }

    PacketBuffer buffer() {
        return THREAD_LOCAL.get();
    }

    void releaseData() {
        THREAD_LOCAL.remove();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Writes the data to the client
     *
     * @return the packet was written successful
     * @param client client to send data
     */
    protected abstract boolean write(T client);
}