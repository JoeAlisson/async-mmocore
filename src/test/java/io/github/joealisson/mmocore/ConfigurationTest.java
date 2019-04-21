package io.github.joealisson.mmocore;

import org.junit.After;
import org.junit.Test;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static org.junit.Assert.assertEquals;

public class ConfigurationTest {

    @Test
    public void testCorrectConfigurations() {
        System.setProperty("async-mmocore.configurationFile", "/async-mmocore.properties");
        ConnectionConfig<AsyncClient> config = new ConnectionConfig<>(null, null, null);

        assertEquals(32192, config.bufferDefaultSize);
        assertEquals(3024, config.bufferSmallSize);
        assertEquals(5048, config.bufferMediumSize);
        assertEquals(16096, config.bufferLargeSize);
        assertEquals(1000, config.bufferPoolSize);
        assertEquals(1000, config.bufferSmallPoolSize);
        assertEquals(500, config.bufferMediumPoolSize);
        assertEquals(250, config.bufferLargePoolSize);
        assertEquals(50 * 1000L, config.shutdownWaitTime);
        assertEquals(6, config.threadPoolSize);
        assertEquals(BIG_ENDIAN, config.byteOrder);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistentConfigurationFile() {
        System.setProperty("async-mmocore.configurationFile", "/async-mmocore-nonexistent.properties");
        ConnectionConfig<AsyncClient> config = new ConnectionConfig<>(null, null, null);
    }

    @Test
    public void testWrongValuesConfigurations() {
        System.setProperty("async-mmocore.configurationFile", "/async-mmocore-wrong.properties");
        ConnectionConfig<AsyncClient> config = new ConnectionConfig<>(null, null, null);

        assertEquals(25, config.bufferLargePoolSize);
        assertEquals(5 * 1000L, config.shutdownWaitTime);
        assertEquals(max(1, getRuntime().availableProcessors() - 2), config.threadPoolSize);
        assertEquals(LITTLE_ENDIAN, config.byteOrder);
    }

    @After
    public void tearDown() {
        System.setProperty("async-mmocore.configurationFile", "");
    }
}
