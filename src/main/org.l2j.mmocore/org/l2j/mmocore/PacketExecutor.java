package org.l2j.mmocore;

/**
 * This class is responsible to execute the incoming packets.
 */
public interface PacketExecutor<T> {

    /**
     * Executes the packet.
     *
     * @param packet the packet to be executed.
     */
	void execute(ReadablePacket<T> packet);
}
