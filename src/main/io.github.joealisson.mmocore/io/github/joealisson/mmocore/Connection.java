package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class Connection<T extends Client<Connection<T>>> {

    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

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
            channel.read(getReadingBuffer(), client, readHandler);
        }
    }

    final void write(byte[] data, int offset, int length, boolean sync) {
        if(!channel.isOpen()) {
            return;
        }

        ByteBuffer buffer = getWritingBuffer(length);
        buffer.put(data, offset, length);
        buffer.flip();
        if(sync) {
            writeSync();
        } else {
            write();
        }
    }

    final void write() {
        if(channel.isOpen() && nonNull(writingBuffer)) {
            channel.write(writingBuffer, client, writeHandler);
        }
    }

    private void writeSync() {
        try {
            int dataSize = client.getDataSentSize();
            int dataSent = 0;
            do {
                dataSent += channel.write(writingBuffer).get();
            } while (dataSent < dataSize);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getLocalizedMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    ByteBuffer getReadingBuffer() {
        if(isNull(readingBuffer)) {
            readingBuffer = client.getResourcePool().getPooledBuffer();
        }
        return readingBuffer;
    }

    private ByteBuffer getWritingBuffer(int length) {
        if(isNull(writingBuffer)) {
            writingBuffer =  client.getResourcePool().getPooledBuffer(length);
        }
        return writingBuffer;
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
            channel.close();
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
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
