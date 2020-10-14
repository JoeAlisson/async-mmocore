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

import io.github.joealisson.mmocore.internal.InternalWritableBuffer;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Math.max;
import static java.util.Objects.isNull;

/**
 * This class represents a Packet that can be sent to clients.
 *
 * All data sent must have a header with 2 bytes and an optional payload.
 *
 * The first and second bytes is a 16 bits integer holding the size of the packet.
 *
 * @author JoeAlisson
 */
public abstract class WritablePacket<T extends Client<Connection<T>>> {

    private static final Map<Class<?>, Integer> packetInfo = new ConcurrentHashMap<>();

    protected WritablePacket() { }

    InternalWritableBuffer writeData(T client) {
        InternalWritableBuffer buffer = choosePacketBuffer(client);

        buffer.position(ConnectionConfig.HEADER_SIZE);
        if(write(client, buffer)) {
            buffer.mark();
            return buffer;
        }
        buffer.releaseResources();
        return null;
    }

    private InternalWritableBuffer choosePacketBuffer(T client) {
        ByteBuffer buffer;
        if(packetInfo.containsKey(getClass())) {
            buffer = client.getResourcePool().getBuffer(packetInfo.get(getClass()));
        } else {
            buffer = client.getResourcePool().getSegmentBuffer();
        }
        return InternalWritableBuffer.dynamicOf(buffer, client.getResourcePool());
    }

    void writeHeaderAndRecord(InternalWritableBuffer buffer, int header) {
        buffer.writeShort(0, (short) header);
        packetInfo.compute(getClass(), (k, v) -> isNull(v) ? header : max(v, header));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Writes the data to the client
     *
     * @return the packet was written successful
     * @param client client to send data
     * @param buffer where the data is written into
     */
    protected abstract boolean write(T client, WritableBuffer buffer);
}