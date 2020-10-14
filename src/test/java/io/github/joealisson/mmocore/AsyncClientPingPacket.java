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

public class AsyncClientPingPacket extends WritablePacket<AsyncClient> {

    @Override
    protected boolean write(AsyncClient client, WritableBuffer buffer) {
        buffer.writeByte(0x01);
        buffer.writeLong(Long.MAX_VALUE);
        buffer.writeDouble(Double.MAX_VALUE);
        buffer.writeInt(Integer.MAX_VALUE);
        buffer.writeFloat(Float.MAX_VALUE);
        buffer.writeShort(Short.MAX_VALUE);
        buffer.writeByte(Byte.MAX_VALUE);
        buffer.writeString("Ping");
        buffer.writeString(null);
        buffer.writeSizedString("Packet");
        buffer.writeSizedString(null);
        buffer.writeByte(true);
        buffer.writeByte(false);
        buffer.writeShort(true);
        buffer.writeShort(false);
        buffer.writeInt(true);
        buffer.writeInt(false);
        buffer.writeBytes(null);
        buffer.writeBytes(new byte[4]);
        return true;
    }
}
