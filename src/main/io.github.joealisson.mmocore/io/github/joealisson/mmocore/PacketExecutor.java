package io.github.joealisson.mmocore;

/**
 * This class is responsible to execute the incoming packets.
 *
 * @author JoeAlisson
 */
@FunctionalInterface
public interface PacketExecutor<T extends Client<Connection<T>>> {

    /**
     * Executes the packet.
     *
     * Its highly recommended to execute long running code or blocking code in a another thread.
     *
     * @param packet the packet to be executed.
     *
     */
	void execute(ReadablePacket<T> packet);
}
