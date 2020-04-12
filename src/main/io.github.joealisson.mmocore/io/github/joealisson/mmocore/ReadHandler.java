package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;

import static io.github.joealisson.mmocore.Client.HEADER_SIZE;
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

        readData(client);
    }

    private void readData(T client) {
        Connection<T> connection = client.getConnection();
        ByteBuffer buffer = connection.getReadingBuffer();
        buffer.flip();

        if (buffer.remaining() < HEADER_SIZE){
            LOGGER.debug("Not enough data to read packet header");
            buffer.compact();
            connection.read();
            return;
        }

        int dataSize = Short.toUnsignedInt(buffer.getShort()) - HEADER_SIZE;

        if(dataSize > buffer.remaining()) {
            LOGGER.debug("Not enough data to read. Packet size {}", dataSize);
            buffer.position(buffer.position() - HEADER_SIZE);
            buffer.compact();
            connection.read();
            return;
        }

        onCompleteRead(client, connection, buffer, dataSize);
    }

    private void onCompleteRead(T client, Connection<T> connection, ByteBuffer buffer, int dataSize) {
        boolean continueReading = true;
        try {
            if (dataSize > 0) {
                parseAndExecutePacket(client, buffer, dataSize);

                if (!buffer.hasRemaining()) {
                    buffer.clear();
                } else {
                    LOGGER.debug("Still data on packet. Trying to read");
                    int remaining = buffer.remaining();
                    buffer.compact();
                    if (remaining >= HEADER_SIZE) {
                        completed(remaining, client);
                        continueReading = false;
                    }
                }
            }
        } catch(Exception e) {
            LOGGER.warn(e.getMessage(), e);
            buffer.clear();
        } finally {
            if(continueReading) {
                connection.read();
            }
        }
    }

    private void parseAndExecutePacket(T client, ByteBuffer incomingBuffer, int dataSize) {
        LOGGER.debug("Trying to parse data");

        byte[] data = new byte[dataSize];
        incomingBuffer.get(data);

        boolean decrypted = client.decrypt(data, 0, dataSize);

        if(decrypted) {
            PacketBuffer buffer = PacketBuffer.of(data, 0);
            ReadablePacket<T> packet = packetHandler.handlePacket(buffer, client);
            LOGGER.debug("Data parsed to packet {}", packet);
            if(nonNull(packet)) {
                packet.init(client, buffer);
                execute(packet);
            }
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
        if(client.isConnected()) {
            client.disconnect();
        }
        if(! (e instanceof ClosedChannelException)) {
            LOGGER.warn(e.getMessage(), e);
        } else {
            LOGGER.debug(e.getMessage(), e);
        }

    }
}