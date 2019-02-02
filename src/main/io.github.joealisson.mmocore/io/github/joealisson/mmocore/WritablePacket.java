package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * This class represents a Packet that can be sent to clients.
 *
 * All data sent must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 */
public abstract class WritablePacket<T extends Client<Connection<T>>> {

    private byte[] staticData;
    protected WritablePacket() { }

    /**
     * Write a <B>String</B> to the buffer with a null termination (\000).
     * Each character is a 16bit char
     *
     * @param text to be written
     * @param buffer where the text will be written
     */
    protected final void writeString(final CharSequence text, final ByteBuffer buffer) {
        if(isNull(text)) {
            buffer.putChar('\000');
            return;
        }
        writeStringWithCharset(text, buffer);
        buffer.putChar('\000');
    }

    private void writeStringWithCharset(CharSequence text, ByteBuffer buffer) {
        Charset charset = buffer.order() == ByteOrder.BIG_ENDIAN  ?  StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE;
        buffer.put(text.toString().getBytes(charset));
    }

    /**
     * Write <B>String</B> to the buffer preceded by a <B>short</B> 16 bit with String length and no null termination.
     * Each character is a 16bit char.
     *
     * @param text to be written
     * @param buffer where the text will be written
     */
    protected final void writeSizedString(final CharSequence text, final ByteBuffer buffer) {
        if(nonNull(text) &&text.length() > 0) {
            buffer.putShort((short) text.length());
            writeStringWithCharset(text, buffer);
        } else {
            buffer.putShort((short) 0);
        }
    }

    int writeData(T client, ByteBuffer buffer) {
        if(hasWritedStaticData()) {
            return writedStaticData(buffer);
        }
        if(write(client, buffer)) {
            int dataPos = buffer.position();
            if(getClass().isAnnotationPresent(StaticPacket.class)) {
                staticData = new byte[dataPos];
                buffer.rewind();
                buffer.get(staticData);
            }
            return dataPos;
        }
        return 0;
    }

    private int writedStaticData(ByteBuffer buffer) {
        buffer.clear();
        buffer.put(staticData);
        return staticData.length;
    }

    private boolean hasWritedStaticData() {
        return getClass().isAnnotationPresent(StaticPacket.class) && nonNull(staticData);
    }

    /**
     *
     * @return the packet size
     */
    protected int size() {
        return -1;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Writes the data into the packet
     *
     * @return the packet was written successful
     * @param client client to send data
     * @param buffer buffer to write data to
     */
    protected abstract boolean write(T client, ByteBuffer buffer);
}