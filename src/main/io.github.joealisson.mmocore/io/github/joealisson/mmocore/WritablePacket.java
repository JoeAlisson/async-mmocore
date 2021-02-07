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

import io.github.joealisson.mmocore.internal.ArrayPacketBuffer;
import io.github.joealisson.mmocore.internal.InternalWritableBuffer;

import static java.util.Objects.nonNull;

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

    private volatile boolean broadcast;
    private ArrayPacketBuffer broadcastCacheBuffer;

    protected WritablePacket() { }

    InternalWritableBuffer writeData(T client) {
        if(broadcast) {
            return writeDataWithCache(client);
        }
        return writeDataToBuffer(client);
    }

    private synchronized InternalWritableBuffer writeDataWithCache(T client) {
        if (nonNull(broadcastCacheBuffer)) {
            return InternalWritableBuffer.dynamicOf(broadcastCacheBuffer, client.getResourcePool());
        } else {
            InternalWritableBuffer buffer = writeDataToBuffer(client);
            if(buffer instanceof ArrayPacketBuffer) {
                broadcastCacheBuffer = (ArrayPacketBuffer) buffer;
                return InternalWritableBuffer.dynamicOf(broadcastCacheBuffer, client.getResourcePool());
            }
            return buffer;
        }
    }

    private InternalWritableBuffer writeDataToBuffer(T client) {
        InternalWritableBuffer buffer = choosePacketBuffer(client);

        buffer.position(ConnectionConfig.HEADER_SIZE);
        if (write(client, buffer)) {
            buffer.mark();
            return buffer;
        }
        buffer.releaseResources();
        return null;
    }

    private InternalWritableBuffer choosePacketBuffer(T client) {
        if(broadcast) {
            return InternalWritableBuffer.arrayBacked(client.getResourcePool());
        }
        return InternalWritableBuffer.dynamicOf(client.getResourcePool().getSegmentBuffer(), client.getResourcePool());
    }

    void writeHeader(InternalWritableBuffer buffer, int header) {
        buffer.writeShort(0, (short) header);
    }

    /**
     * Mark this packet as broadcast. A broadcast packet is sent to more than one client.
     *
     * Caution: This method should be called before {@link Client#writePacket(WritablePacket)}
     *
     * A broadcast packet will create a Buffer cache where the data is written once and only the copy is sent to the client.
     * note: Each copy will be encrypted to each client
     *
     * @param broadcast true if the packet is sent to more than one client
     */
    public void sendInBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    /**
     * If this method returns true, the packet will be considered disposable.
     *
     * @param client client to send data to
     * @return if the packet is disposable or not.
     */
    public boolean canBeDropped(T client) {
        return false;
    }

    /**
     * Writes the data to the client
     *
     * @return the packet was written successful
     * @param client client to send data
     * @param buffer where the data is written into
     */
    protected abstract boolean write(T client, WritableBuffer buffer);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    }