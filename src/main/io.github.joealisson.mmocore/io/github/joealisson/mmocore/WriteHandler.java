package io.github.joealisson.mmocore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.CompletionHandler;

class WriteHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T> {

    private  static final Logger logger = LoggerFactory.getLogger(WriteHandler.class);

    @Override
    public void completed(Integer result, T client) {
        if(result < 0) {
            logger.warn("Couldn't send data to client {}", client);
            if(client.isConnected()) {
                client.disconnect();
            }
            return;
        }

        if(result < client.getDataSentSize()) {
            logger.debug("Still data to send. Trying to send");
            client.resumeSend(result);
        } else {
            client.finishWriting();
        }
    }

    @Override
    public void failed(Throwable e, T client) {
        if(client.isConnected()) {
            client.disconnect();
        }
        if(! (e instanceof IOException)) {
            logger.warn(e.getLocalizedMessage(), e);
        }
    }
}
