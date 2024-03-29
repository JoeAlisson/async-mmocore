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

import org.junit.After;
import org.junit.Test;

import java.net.InetSocketAddress;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author JoeAlisson
 */
public class ConfigurationTest {

    @Test
    public void testCorrectConfigurations() {
        System.setProperty("async-mmocore.configurationFile", "/async-mmocore.properties");
        ConnectionConfig config = new ConnectionConfig(null);
        config.complete();
        assertTrue(config.resourcePool.bufferPoolSize() >= 2);
        assertEquals(0.2f, config.initBufferPoolFactor, 0);
        assertEquals(50 * 1000L, config.shutdownWaitTime);
        assertEquals(6, config.threadPoolSize);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentConfigurationFile() {
        System.setProperty("async-mmocore.configurationFile", "/async-mmocore-nonexistent.properties");
        new ConnectionConfig(null);
    }

    @Test
    public void testWrongValuesConfigurations() {
        System.setProperty("async-mmocore.configurationFile", "/async-mmocore-wrong.properties");
        ConnectionConfig config = new ConnectionConfig(null);
        config.complete();
        assertEquals(5 * 1000L, config.shutdownWaitTime);
        assertEquals(max(2, getRuntime().availableProcessors() - 2), config.threadPoolSize);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderThreadPriorityLower() {
        var socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).threadPriority(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderThreadPriorityHigher() {
        var socketAddress = new InetSocketAddress("127.0.0.1", 9090);
        ConnectionBuilder.create(socketAddress, AsyncClient::new, null, null).threadPriority(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectorThreadPriorityLower() {
       Connector.create(AsyncClient::new, null, null).threadPriority(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConnectorThreadPriorityHigher() {
       Connector.create(AsyncClient::new, null, null).threadPriority(100);
    }

    @After
    public void tearDown() {
        System.setProperty("async-mmocore.configurationFile", "");
    }
}
