package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;

import static io.github.joealisson.mmocore.Client.HEADER_SIZE;
import static java.util.Objects.nonNull;

class ReadHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T> {

    private static final Logger logger = LoggerFactory.getLogger(ReadHandler.class);

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

        logger.debug("Reading {} from {}", bytesRead, client);
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
            logger.debug("Not enough data to read packet header");
            buffer.compact();
            connection.read();
            return;
        }

        int dataSize = Short.toUnsignedInt(buffer.getShort()) - HEADER_SIZE;

        if(dataSize > buffer.remaining()) {
            logger.debug("Not enough data to read. Packet size {}", dataSize);
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
                    logger.debug("Still data on packet. Trying to read");
                    int remaining = buffer.remaining();
                    buffer.compact();
                    if (remaining >= HEADER_SIZE) {
                        completed(remaining, client);
                        continueReading = false;
                    }
                }
            }
        } catch(Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            buffer.clear();
        } finally {
            if(continueReading) {
                connection.read();
            }
        }
    }

    private void parseAndExecutePacket(T client, ByteBuffer incomingBuffer, int dataSize) {
        logger.debug("Trying to parse data");

        ByteBuffer buffer = client.getResourcePool().getPooledBuffer(dataSize);
        int limit = incomingBuffer.limit();
        incomingBuffer.limit(incomingBuffer.position() + dataSize);

        buffer.put(incomingBuffer);
        incomingBuffer.limit(limit);
        boolean decrypted = client.decrypt(buffer.array(), 0, dataSize);

        if(decrypted) {
            buffer.flip();
            ReadablePacket<T> packet = packetHandler.handlePacket(buffer, client);
            logger.debug("Data parsed to packet {}", packet);
            if(nonNull(packet)) {
                packet.client = client;
                execute(packet, buffer);
            }
        }
    }

    private void execute(ReadablePacket<T> packet, ByteBuffer buffer) {
        if(packet.read(buffer)) {
            logger.debug("packet {} was read from client {}", packet, packet.client);
            executor.execute(packet);
        }
     }

    @Override
    public void failed(Throwable e, T client) {
        if(client.isConnected()) {
            client.disconnect();
        }
        if(! (e instanceof IOException)) {
            // client just closes the connection, doesn't need to be logged
            logger.warn(e.getLocalizedMessage(), e);
        }

    }
}