package org.l2j.mmocore;

import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

import static java.util.Objects.nonNull;

class ReadHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T> {

    static final int HEADER_SIZE = 2;

    private final PacketHandler<T> packetHandler;
    private final PacketExecutor<T> executor;

    ReadHandler(PacketHandler<T> packetHandler, PacketExecutor<T> executor) {
        this.packetHandler = packetHandler;
        this.executor =  executor;
    }

    @Override
    public void completed(Integer bytesRead, T client) {
        Connection<T> connection = client.getConnection();
        if(bytesRead < 0 ) {
            client.disconnect();
            return;
        }

        var buffer = connection.getReadingBuffer();
        buffer.flip();

        if (buffer.remaining() < HEADER_SIZE){
            buffer.compact();
            connection.read();
            return;
        }

        int dataSize = Short.toUnsignedInt(buffer.getShort()) - HEADER_SIZE;

        if(dataSize > buffer.remaining()) {
            buffer.position(buffer.position() - HEADER_SIZE);
            buffer.compact();
            connection.read();
            return;
        }

        if(dataSize > 0) {
            parseAndExecutePacket(client, buffer, dataSize);
        }

        if(!buffer.hasRemaining()) {
            buffer.clear();
        } else {
            int remaining = buffer.remaining();
            buffer.compact();
            if(remaining >= HEADER_SIZE) {
                completed(remaining, client);
                return;
            }
        }
        connection.read();
    }

    private void parseAndExecutePacket(T client, ByteBuffer buffer, int dataSize) {
        byte[] data = new byte[dataSize];

        buffer.get(data, 0, dataSize);
        boolean decrypted = client.decrypt(data, 0, dataSize);

        if(decrypted) {
            DataWrapper wrapper = DataWrapper.wrap(data);
            ReadablePacket<T> packet = packetHandler.handlePacket(wrapper, client);
            execute(client, packet, wrapper);
        }
    }

    private void execute(T client, ReadablePacket<T> packet, DataWrapper wrapper) {
        if(nonNull(packet)) {
            packet.client = client;
            packet.data = wrapper.data;
            packet.dataIndex = wrapper.dataIndex;
            if(packet.read()) {
                executor.execute(packet);
            }
        }
     }

    @Override
    public void failed(Throwable exc, T client) {
        client.disconnect();
        exc.printStackTrace();
    }
}
