package org.l2j.mmocore;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.isNull;

public class Connection<T extends Client<Connection<T>>> {

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

    public void setClient(T client) {
        this.client = client;
    }

    final void read() {
        if(channel.isOpen()) {
            channel.read(getReadingBuffer(), client, readHandler);
        }
    }

    final void write(byte[] data, int offset, int limit, boolean sync) {
        if(!channel.isOpen()) {
            return;
        }

        ByteBuffer buffer = getWritingBuffer();
        buffer.put(data, offset, limit);
        buffer.flip();
        if(sync) {
            writeSync();
        } else {
            write();
        }
    }


    final void write() {
        if(channel.isOpen()) {
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
            e.printStackTrace();
        }
    }

    ByteBuffer getReadingBuffer() {
        if(isNull(readingBuffer)) {
            readingBuffer = ResourcePool.getPooledBuffer();
        }
        return readingBuffer;
    }

    private ByteBuffer getWritingBuffer() {
        if(isNull(writingBuffer)) {
            writingBuffer =  ResourcePool.getPooledBuffer();
        }
        return writingBuffer;
    }

    private void releaseReadingBuffer() {
        ResourcePool.recycleBuffer(readingBuffer);
        readingBuffer=null;
    }

    void releaseWritingBuffer() {
        ResourcePool.recycleBuffer(writingBuffer);
        writingBuffer = null;
    }

    void close() {
        releaseReadingBuffer();
        releaseWritingBuffer();
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
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
        return channel.isOpen();
    }
}
