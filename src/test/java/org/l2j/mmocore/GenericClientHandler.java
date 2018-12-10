package org.l2j.mmocore;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GenericClientHandler implements PacketHandler<AsyncClient>, PacketExecutor<AsyncClient> {

    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 2, 15L,TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());


    @Override
    public ReadablePacket<AsyncClient> handlePacket(DataWrapper wrapper, AsyncClient client) {
        int opcode = Byte.toUnsignedInt(wrapper.get());
        ReadablePacket<AsyncClient> packet = null;
        if(opcode == 0x01) {
            packet = new AsyncServerPingPacket();
        } else if(opcode == 0x02) {
            packet = new AsyncClientPongPacket();
        } else if(opcode == 0x03) {
            packet = new AsyncServerClosePacket();
        }
        return packet;
    }

    @Override
    public void execute(ReadablePacket<AsyncClient> packet) {
        threadPool.execute(packet);
    }
}
