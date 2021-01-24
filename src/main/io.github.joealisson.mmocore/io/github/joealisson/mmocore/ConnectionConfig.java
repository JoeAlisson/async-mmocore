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

import io.github.joealisson.mmocore.internal.BufferPool;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.util.Objects.nonNull;

/**
 * @author JoeAlisson
 */
class ConnectionConfig<T extends Client<Connection<T>>> {

    public static final int HEADER_SIZE = 2;

    private static final int MINIMUM_POOL_GROUPS = 3;
    private static final Pattern BUFFER_POOL_PROPERTY = Pattern.compile("(bufferPool\\.\\w+?\\.)size", Pattern.CASE_INSENSITIVE);

    ClientFactory<T> clientFactory;
    ConnectionFilter acceptFilter;
    SocketAddress address;
    Map<Integer, BufferPool> bufferPools = new HashMap<>(4);

    float initBufferPoolFactor;
    long shutdownWaitTime = 5000;
    int threadPoolSize;
    boolean useNagle;
    int bufferSegmentSize = 256;

    ConnectionConfig(SocketAddress address, ClientFactory<T> factory) {
        this.address = address;
        this.clientFactory = factory;
        threadPoolSize = max(1, getRuntime().availableProcessors() - 2);
        bufferPools.put(HEADER_SIZE, new BufferPool(100, HEADER_SIZE));

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
        shutdownWaitTime = parseInt(properties, "shutdownWaitTime", 5) * 1000L;
        threadPoolSize = parseInt(properties, "threadPoolSize", threadPoolSize);
        bufferSegmentSize = parseInt(properties, "bufferSegmentSize", bufferSegmentSize);
        initBufferPoolFactor = parseFloat(properties, "bufferPool.initFactor", 0);

        properties.stringPropertyNames().forEach(property -> {
            Matcher matcher = BUFFER_POOL_PROPERTY.matcher(property);
            if(matcher.matches()) {
                int size = parseInt(properties, property, 10);
                int bufferSize = parseInt(properties, matcher.group(1) + "bufferSize", 1024);
                newBufferGroup(size, bufferSize);
            }
        });

        newBufferGroup(100, bufferSegmentSize);
    }

    private int parseInt(Properties properties, String propertyName, int defaultValue) {
        try{
            return Integer.parseInt(properties.getProperty(propertyName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float parseFloat(Properties properties, String propertyName, float defaultValue) {
        try{
            return Float.parseFloat(properties.getProperty(propertyName));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public void newBufferGroup(int groupSize, int bufferSize) {
        if(!bufferPools.containsKey(bufferSize)) {
            bufferPools.put(bufferSize, new BufferPool(groupSize, bufferSize));
        }
    }

    public ConnectionConfig<T> complete() {
        completeBuffersPool();
        if(initBufferPoolFactor > 0) {
            bufferPools.values().forEach(pool -> pool.initialize(initBufferPoolFactor));
        }
        return this;
    }

    private void completeBuffersPool() {
        int missingPools = MINIMUM_POOL_GROUPS - bufferPools.size();

        for (int i = 0; i < missingPools; i++) {
            int bufferSize = 16384 << i;
            bufferPools.put(bufferSize,new BufferPool(10, bufferSize));
        }
    }
}