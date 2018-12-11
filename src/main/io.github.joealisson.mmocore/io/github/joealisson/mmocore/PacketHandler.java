package io.github.joealisson.mmocore;

/**
 * This class is responsible to handler the incoming data. Converting it to a packet.
 */
public interface PacketHandler<T extends Client<Connection<T>>> {

    /**
     * Convert the data into a packet.
     *
     * @param data - the data to be converted.
     * @param client - the client who sends the data
     *
     * @return A Packet related to the data received.
     */
	ReadablePacket<T> handlePacket(DataWrapper data, T client);
}
