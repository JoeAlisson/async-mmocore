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

import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.fail;

public class CommunicationTest {

    private static final AtomicBoolean shutdown = new AtomicBoolean(false);
    private static boolean success = true;
    private ConnectionBuilder<AsyncClient> builder;
    private Connector<AsyncClient> connector;
    private final InetSocketAddress listenAddress = new InetSocketAddress(9090);
    private ConnectionHandler<AsyncClient> connectionHandler;

    static void shutdown(boolean success) {
        shutdown.getAndSet(true);
        CommunicationTest.success = success;
    }

    @Before
    public void SetUp() {
        GenericClientHandler handler = new GenericClientHandler();

        builder = ConnectionBuilder.create(listenAddress, AsyncClient::new, handler, handler).filter(channel -> true).threadPoolSize(2).useNagle(false)
                .shutdownWaitTime(500).addBufferPool(10,300).initBufferPoolFactor(0.2f).bufferSegmentSize(256);
        connector = Connector.create(AsyncClient::new, handler, handler).addBufferPool(10, 300).initBufferPoolFactor(0.2f).bufferSegmentSize(128);

    }

    @Test
    public void testIntegration() throws IOException, ExecutionException, InterruptedException {
        connectionHandler = builder.build();
        connectionHandler.start();

        AsyncClient client = connector.connect("localhost", 9090);
        client.sendPacket(new AsyncClientPingPacket());

        Awaitility.waitAtMost(10, TimeUnit.SECONDS).untilTrue(shutdown);

        connectionHandler.shutdown();
        connectionHandler.join();
        if(!success) {
            fail();
        }
    }

    @After
    public void tearDown() {
        if(!shutdown.get()) {
            connectionHandler.shutdown();
        }
    }
}
