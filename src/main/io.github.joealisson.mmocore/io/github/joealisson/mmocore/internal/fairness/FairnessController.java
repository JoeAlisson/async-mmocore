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

import java.util.function.Consumer;

/**
 * @author JoeAlisson
 */
public class FairnessController {

    private FairnessStrategy strategy;

    private FairnessController() {
        // only construct by init
    }

    /**
     * Create a Fairness Controller using the fairnessBuckets
     *
     * @param fairnessBuckets the amount of buckets used in FairnessController
     * @return the fairness Controller
     */
    public static FairnessController init(int fairnessBuckets) {
        FairnessController controller = new FairnessController();
        if(fairnessBuckets <= 1) {
            controller.strategy = new SingleBucketStrategy();
        } else {
            controller.strategy = new MultiBucketStrategy(fairnessBuckets);
        }
        return controller;
    }

    /**
     * Add the client to the fairnessController and execute the action to the next client
     *
     * @param client the client to be added to Fairness Controller
     * @param action the action to execute to the next fair client
     */
    public void nextFairAction(Client<?> client, Consumer<Client<?>> action) {
        strategy.doNextAction(client, action);
    }
}
