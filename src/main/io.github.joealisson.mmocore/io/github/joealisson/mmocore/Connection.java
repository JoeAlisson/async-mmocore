package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public class Connection<T extends Client<Connection<T>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private final AsynchronousSocketChannel channel;
    private final ReadHandler<T> readHandler;
    private final WriteHandler<T> writeHandler;
    private T client;

    private ByteBuffer readingBuffer;
    private ByteBuffer writingBuffer;

    Connection(AsynchronousSocketChannel channel, ReadHandler<T> readHandler, WriteHandler<T> writeHandler) {
        this.channel = channel;
        this.readHandler = readHandler;
        this.writeHandler = writeHandler;
    }

    void setClient(T client) {
        this.client = client;
    }

    final void read() {
        if(channel.isOpen()) {
            channel.read(readingBuffer, client, readHandler);
        }
    }

    final void readHeader() {
        if(channel.isOpen()) {
            releaseReadingBuffer();
            readingBuffer = client.getResourcePool().getHeaderBuffer();
            read();
        }
    }

    void read(int size) {
        if(channel.isOpen()) {
            readingBuffer = client.getResourcePool().recycleAndGetNew(readingBuffer, size);
            read();
        }
    }

    final void write(byte[] data, int size) {
        if(!channel.isOpen()) {
            return;
        }
        writingBuffer = client.getResourcePool().recycleAndGetNew(writingBuffer, size);
        writingBuffer.put(data, 0, size);
        writingBuffer.flip();
        write();
    }

    final void write() {
        if(channel.isOpen() && nonNull(writingBuffer)) {
            channel.write(writingBuffer, client, writeHandler);
        }
    }

    ByteBuffer getReadingBuffer() {
        return readingBuffer;
    }

    private void releaseReadingBuffer() {
        client.getResourcePool().recycleBuffer(readingBuffer);
        readingBuffer=null;
    }

    void releaseWritingBuffer() {
        client.getResourcePool().recycleBuffer(writingBuffer);
        writingBuffer = null;
    }

    void close() {
        releaseReadingBuffer();
        releaseWritingBuffer();
        try {
            if(channel.isOpen()) {
                channel.shutdownInput();
                channel.shutdownOutput();
                channel.close();
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    String getRemoteAddress() {
        try {
            InetSocketAddress address = (InetSocketAddress) channel.getRemoteAddress();
            return address.getAddress().getHostAddress();
        } catch (IOException e) {
            return "";
        }
    }

    boolean isOpen() {
        try {
            return channel.isOpen() && nonNull(channel.getRemoteAddress());
        } catch (Exception e) {
            return false;
        }
    }
}
