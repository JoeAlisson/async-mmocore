package io.github.joealisson.mmocore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This class represents a Packet received from the client.
 *
 * All data received must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 */
public abstract class ReadablePacket<T extends Client<Connection<T>>> implements Runnable {

    protected T client;

    protected ReadablePacket() {
        // no direct instances
    }

    /**
     * Reads <B>String</B> from the buffer.
     * @param buffer that contains the String
     * @return String read
     */
    protected final String readString(ByteBuffer buffer)  {
        StringBuilder builder = new StringBuilder();
        char c;
        while((c = buffer.getChar()) != '\000') {
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     *
     * Reads a predefined length <B>String</B> from the buffer.
     * @param buffer that contains the String
     * @return String read
     */
    protected final String readSizedString(ByteBuffer buffer) {
        int size = buffer.getShort() * 2;
        byte[] data = new byte[size];
        buffer.get(data);
        Charset charset = buffer.order() == ByteOrder.BIG_ENDIAN ? StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE;
        return new String(data, 0, size, charset);
    }

    public T getClient() {
        return client;
    }

    protected abstract boolean read(ByteBuffer buffer);
}