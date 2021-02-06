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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
public class Connection<T extends Client<Connection<T>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);

    private final AsynchronousSocketChannel channel;
    private final ReadHandler<T> readHandler;
    private final WriteHandler<T> writeHandler;
    private final ConnectionConfig config;
    private T client;

    private ByteBuffer readingBuffer;
    private ByteBuffer[] writingBuffers;

    Connection(AsynchronousSocketChannel channel, ReadHandler<T> readHandler, WriteHandler<T> writeHandler, ConnectionConfig config) {
        this.channel = channel;
        this.readHandler = readHandler;
        this.writeHandler = writeHandler;
        this.config = config;
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
            readingBuffer = config.resourcePool.getHeaderBuffer();
            read();
        }
    }

    void read(int size) {
        if(channel.isOpen()) {
            readingBuffer = config.resourcePool.recycleAndGetNew(readingBuffer, size);
            read();
        }
    }

    final boolean write(ByteBuffer[] buffers) {
        if(!channel.isOpen()) {
            return false;
        }
        writingBuffers = buffers;
        write();
        return true;
    }

    final void write() {
        if(channel.isOpen() && nonNull(writingBuffers)) {
            channel.write(writingBuffers, 0, writingBuffers.length, -1, TimeUnit.MILLISECONDS,  client, writeHandler);
        } else if(nonNull(client)) {
            client.finishWriting();
        }
    }

    ByteBuffer getReadingBuffer() {
        return readingBuffer;
    }

    private void releaseReadingBuffer() {
        if(nonNull(readingBuffer)) {
            config.resourcePool.recycleBuffer(readingBuffer);
            readingBuffer = null;
        }
    }

    boolean releaseWritingBuffer() {
        boolean released = false;
        if(nonNull(writingBuffers)) {
            for (ByteBuffer buffer : writingBuffers) {
                config.resourcePool.recycleBuffer(buffer);
                released = true;
            }
            writingBuffers = null;
        }
        return released;
    }

    void close() {
        releaseReadingBuffer();
        releaseWritingBuffer();
        try {
            if(channel.isOpen()) {
                channel.close();
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            client = null;
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

    public ResourcePool getResourcePool() {
        return config.resourcePool;
    }

    public int disposePacketThreshold() {
        return config.disposePacketThreshold;
    }
}
