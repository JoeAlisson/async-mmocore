package org.l2j.mmocore.async;

import org.l2j.mmocore.*;
import org.l2j.mmocore.packet.AsyncPingPacket;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GenericClientHandler implements ClientFactory<AsyncClient>, PacketHandler<AsyncClient>, PacketExecutor<AsyncClient> {

    ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 2, 15L,TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory());

    @Override
    public AsyncClient create(Connection<AsyncClient> connection) {
        return new AsyncClient(connection);
    }

    @Override
    public ReadablePacket<AsyncClient> handlePacket(DataWrapper wrapper, AsyncClient client) {
        var opcode = Byte.toUnsignedInt(wrapper.get());
        ReadablePacket<AsyncClient> packet = null;
        switch (opcode) {
            case 0x01:
                packet = new AsyncPingPacket();
        }
        return packet;
    }

    @Override
    public void execute(ReadablePacket<AsyncClient> packet) {
        threadPool.execute(packet);
    }
}
