package org.l2j.mmocore;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The filter of incoming connections.
 */
public interface ConnectionFilter {

    /**
     * This method must decide if a Connection can be accepted or not.
     *
     * @param channel - the channel to be filtered
     *
     * @return if a the channel is acceptable.
     */
	boolean accept(AsynchronousSocketChannel channel);
}
