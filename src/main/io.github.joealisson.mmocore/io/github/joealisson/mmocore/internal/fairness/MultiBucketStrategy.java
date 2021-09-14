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
package io.github.joealisson.mmocore.internal.fairness;

import io.github.joealisson.mmocore.Client;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * @author JoeAlisson
 */
class MultiBucketStrategy implements FairnessStrategy {

    private final Queue<Client<?>>[] readyBuckets;
    private final int fairnessBuckets;
    private int nextOffer;
    private int nextPoll;

    @SuppressWarnings("unchecked")
    MultiBucketStrategy(int fairnessBuckets) {
        readyBuckets = new ConcurrentLinkedQueue[fairnessBuckets];
        this.fairnessBuckets = fairnessBuckets;
        for (int i = 0; i < fairnessBuckets; i++) {
            readyBuckets[i] = new ConcurrentLinkedQueue<>();
        }
    }

    @Override
    public void doNextAction(Client<?> client, Consumer<Client<?>> action) {
        int offer = nextOffer++ % fairnessBuckets;
        Queue<Client<?>> nextBucket = readyBuckets[offer];
        nextBucket.offer(client);

        int poll = nextPoll++ % fairnessBuckets;

        nextBucket = readyBuckets[poll];
        Client<?> next = nextBucket.poll();
        if(next != null) {
            action.accept(next);
        }
    }
}
