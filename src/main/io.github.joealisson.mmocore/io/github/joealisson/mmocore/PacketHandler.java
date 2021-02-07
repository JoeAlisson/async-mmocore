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

/**
 * This class is responsible to handler the incoming data. Converting it to a packet.
 *
 * @author JoeAlisson
 */
@FunctionalInterface
public interface PacketHandler<T extends Client<Connection<T>>> {

    /**
     * Convert the data into a packet.
     *
     * @param buffer - the buffer with data to be converted.
     * @param client - the client who sends the data
     *
     * @return A Packet related to the data received.
     */
	ReadablePacket<T> handlePacket(ReadableBuffer buffer, T client);
}
