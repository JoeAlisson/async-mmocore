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

import static java.util.Objects.nonNull;

public class AsyncClient extends Client<Connection<AsyncClient>> {

    public PacketBuffer receivedPacket;

    public AsyncClient(Connection<AsyncClient> connection) {
        super(connection);
    }

    @Override
    public int encryptedSize(int dataSize) {
        return dataSize;
    }

    @Override
    public boolean decrypt(byte[] data, int offset, int size) {
        return true;
    }

    @Override
    public byte[] encrypt(byte[] data, int offset, int size) {
        return data;
    }

    @Override
    protected void onDisconnection() {

    }

    @Override
    public void onConnected() {

    }

    public void sendPacket(WritablePacket<AsyncClient> packet) {
        if(nonNull(packet)) {
            System.out.println("Sending " + packet.toString());
        }
        writePacket(packet);
    }
}
