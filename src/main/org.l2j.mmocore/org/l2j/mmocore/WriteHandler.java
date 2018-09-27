package org.l2j.mmocore;

import java.nio.channels.CompletionHandler;

class WriteHandler<T extends Client<Connection<T>>> implements CompletionHandler<Integer, T> {

    @Override
    public void completed(Integer result, T client) {
        if(result < 0) {
            client.disconnect();
            return;
        }

        Connection connection = client.getConnection();

        if(result < client.getDataSentSize()) {
            client.resumeSend(result);
        } else {
            connection.releaseWritingBuffer();
            client.tryWriteNextPacket();
        }
        
    }

    @Override
    public void failed(Throwable exc, T client) {
        exc.printStackTrace();
        client.disconnect();
    }
}
