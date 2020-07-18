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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

/**
 * @author JoeAlisson
 */
class WriteHandler<T extends Client<Connection<T>>> implements CompletionHandler<Long, T> {

    private  static final Logger LOGGER = LoggerFactory.getLogger(WriteHandler.class);

    @Override
    public void completed(Long result, T client) {
        if(result < 0) {
            LOGGER.warn("Couldn't send data to client {}", client);
            if(client.isConnected()) {
                client.disconnect();
            }
            return;
        }

        if(result < client.getDataSentSize()) {
            LOGGER.debug("Still data to send. Trying to send");
            client.resumeSend(result);
        } else {
            client.finishWriting();
        }
    }

    @Override
    public void failed(Throwable e, T client) {
        if(! (e instanceof IOException)) {
            LOGGER.warn(e.getMessage(), e);
        }
        client.disconnect();
    }
}
