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
package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import static io.github.joealisson.mmocore.ConnectionConfig.HEADER_SIZE;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
class ReadHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadHandler.class);

    private final PacketHandler<T> packetHandler;
    private final PacketExecutor<T> executor;

    ReadHandler(PacketHandler<T> packetHandler, PacketExecutor<T> executor) {
        this.packetHandler = packetHandler;
        this.executor =  executor;
    }

    @Override
    public void completed(Integer bytesRead, T client) {
        if(!client.isConnected()) {
            return;
        }

        LOGGER.debug("Reading {} from {}", bytesRead, client);
        if(bytesRead < 0 ) {
            client.disconnect();
            return;
        }
        
        if(bytesRead < client.getExpectedReadSize()) {
            client.resumeRead(bytesRead);
            return;
        }

        if(client.isReadingPayload()) {
            handlePayload(client);
        } else {
            handleHeader(client);
        }
    }

    private void handleHeader(T client) {
        ByteBuffer buffer = client.getConnection().getReadingBuffer();
        buffer.flip();
        int dataSize = Short.toUnsignedInt(buffer.getShort()) - HEADER_SIZE;
        if(dataSize > 0) {
            client.readPayload(dataSize);
        } else {
            client.read();
        }
    }

    private void handlePayload(T client) {
        ByteBuffer buffer = client.getConnection().getReadingBuffer();
        buffer.flip();
        parseAndExecutePacket(client, buffer);
        client.read();
    }

    private void parseAndExecutePacket(T client, ByteBuffer incomingBuffer) {
        LOGGER.debug("Trying to parse data");

        try {
            ReadableBuffer buffer = ReadableBuffer.of(incomingBuffer);
            boolean decrypted = client.decrypt(buffer, 0, buffer.remaining());

            if (decrypted) {
                ReadablePacket<T> packet = packetHandler.handlePacket(buffer, client);
                LOGGER.debug("Data parsed to packet {}", packet);
                if (nonNull(packet)) {
                    packet.init(client, buffer);
                    execute(packet);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private void execute(ReadablePacket<T> packet) {
        if(packet.read()) {
            LOGGER.debug("packet {} was read from client {}", packet, packet.client);
            executor.execute(packet);
        }
     }

    @Override
    public void failed(Throwable e, T client) {
        LOGGER.debug("Failed to read from {}", client, e);
        client.disconnect();
    }
}