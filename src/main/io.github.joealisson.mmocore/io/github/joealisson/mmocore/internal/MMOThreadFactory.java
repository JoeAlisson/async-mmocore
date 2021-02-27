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
package io.github.joealisson.mmocore.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author JoeAlisson
 */
public class MMOThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final int priority;

    public MMOThreadFactory(String name, int priority){
        namePrefix = name + "-MMO-pool-" +poolNumber.getAndIncrement() + "-thread-";
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(null, r,namePrefix +threadNumber.getAndIncrement(), 0);
        thread.setPriority(priority);
        thread.setDaemon(false);
        return thread;
    }
}
