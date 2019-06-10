package io.github.joealisson.mmocore;

/**
 * This class is responsible to handler the incoming data. Converting it to a packet.
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
	ReadablePacket<T> handlePacket(PacketBuffer buffer, T client);
}
