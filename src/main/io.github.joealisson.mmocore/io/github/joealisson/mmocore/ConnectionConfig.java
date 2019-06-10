package io.github.joealisson.mmocore;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.Objects.nonNull;

class ConnectionConfig<T extends Client<Connection<T>>> {

    int bufferDefaultSize = 8 * 1024;
    int bufferSmallSize = 1024;
    int bufferMediumSize = 2 * 1024;
    int bufferLargeSize = 4 * 1024;
    int bufferPoolSize = 100;
    int bufferSmallPoolSize = 100;
    int bufferMediumPoolSize = 50;
    int bufferLargePoolSize = 25;
    long shutdownWaitTime = 5000;
    int threadPoolSize;
    boolean useNagle;

    ClientFactory<T> clientFactory;
    ConnectionFilter acceptFilter;
    ReadHandler<T> readHandler;
    WriteHandler<T> writeHandler;
    SocketAddress address;

    ConnectionConfig(SocketAddress address, ClientFactory<T> factory, ReadHandler<T> readHandler) {
        this.address = address;
        this.clientFactory = factory;
        this.readHandler = readHandler;
        this.writeHandler = new WriteHandler<>();
        threadPoolSize = max(1, getRuntime().availableProcessors() - 2);

        String systemProperty = System.getProperty("async-mmocore.configurationFile");
        if(nonNull(systemProperty) && !systemProperty.trim().isEmpty()) {
            loadProperties(systemProperty);
        }
    }

    private void loadProperties(String propertyFileName) {
        final Path path = Paths.get(propertyFileName);

        try(final InputStream inputStream = Files.isRegularFile(path) ? Files.newInputStream(path) : getClass().getResourceAsStream(propertyFileName)) {
            if(nonNull(inputStream)) {
                Properties properties = new Properties();
                properties.load(inputStream);
                configure(properties);
            } else {
                throw new IllegalArgumentException("Cannot find property file: " + propertyFileName);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read property file", e);
        }
    }

    private void configure(Properties properties) {
        bufferDefaultSize = parseInt(properties, "bufferDefaultSize",  8 * 1024);
        bufferSmallSize = parseInt(properties, "bufferSmallSize", 1024);
        bufferMediumSize = parseInt(properties, "bufferMediumSize", 2 * 1024);
        bufferLargeSize = parseInt(properties, "bufferLargeSize", 4 * 1024);
        bufferPoolSize = parseInt(properties, "bufferPoolSize", 100);
        bufferSmallPoolSize = parseInt(properties, "bufferSmallPoolSize", 100);
        bufferMediumPoolSize = parseInt(properties, "bufferMediumPoolSize", 50);
        bufferLargePoolSize = parseInt(properties, "bufferLargePoolSize", 25);
        shutdownWaitTime = parseInt(properties, "shutdownWaitTime", 5) * 1000L;
        threadPoolSize = parseInt(properties, "threadPoolSize", threadPoolSize);
    }

    private int parseInt(Properties properties, String propertyName, int defaultValue) {
        try{
            return Integer.parseInt(properties.getProperty(propertyName));
        } catch (Exception e) {
            return defaultValue;
        }
    }
}