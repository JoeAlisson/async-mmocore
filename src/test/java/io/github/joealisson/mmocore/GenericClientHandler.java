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

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author JoeAlisson
 */
public class GenericClientHandler implements PacketHandler<AsyncClient>, PacketExecutor<AsyncClient> {

    private ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 2, 15L,TimeUnit.SECONDS, new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());

    @Override
    public ReadablePacket<AsyncClient> handlePacket(ReadableBuffer buffer, AsyncClient client) {
        int opcode = Byte.toUnsignedInt(buffer.readByte());
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
                       bytes = new byte[buffer.remaining()];
                       buffer.readBytes(bytes);
                    }
                }
            }
            packet = new AsyncClientClosedConnection(bytes);
        }
        else if(opcode == 0x10) {
            packet = new AsyncClientBroadcastReceiverPacket();
        }
        return packet;
    }

    @Override
    public void execute(ReadablePacket<AsyncClient> packet) {
        threadPool.execute(packet);
    }
}
