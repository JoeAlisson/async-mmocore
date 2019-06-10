package io.github.joealisson.mmocore;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GenericClientHandler implements PacketHandler<AsyncClient>, PacketExecutor<AsyncClient> {

    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 2, 15L,TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());

    @Override
    public ReadablePacket<AsyncClient> handlePacket(PacketBuffer buffer, AsyncClient client) {
        int opcode = Byte.toUnsignedInt(buffer.read());
        ReadablePacket<AsyncClient> packet = null;
        if(opcode == 0x01) {
            packet = new AsyncServerPingPacket();
        } else if(opcode == 0x02) {
            packet = new AsyncClientPongPacket();
        } else if(opcode == 0x03) {
            packet = new AsyncServerClosePacket();
        } else if (opcode == 0x04) {
            byte[] bytes = new byte[0];
            if(buffer.remaining() >= 2) {
                short op2 =  buffer.readShort();
                if(op2 == 0x01) {
                    int op3 = buffer.readInt();
                    if(op3 == 0x02) {
                       bytes =  buffer.expose();
                    }
                }
            }
            packet = new AsyncClientClosedConnection(bytes);
        }
        return packet;
    }

    @Override
    public void execute(ReadablePacket<AsyncClient> packet) {
        threadPool.execute(packet);
    }
}
