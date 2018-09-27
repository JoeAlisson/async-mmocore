package org.l2j.mmocore.async;

import org.l2j.mmocore.ConnectionHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class AsyncMmoCoreServerTest {

    public static void main(String[] args) throws IOException {
        AsyncMmoCoreServerTest app = new AsyncMmoCoreServerTest();
        app.start();
    }

    private void start() throws IOException {
        System.out.println("Iniciando Servidor porta 8080");
        GenericClientHandler handler = new GenericClientHandler();
        ConnectionHandler<AsyncClient> connectionHandler = new ConnectionHandler<>(new InetSocketAddress("10.89.5.106", 8080), false, 4, handler, handler, handler, null);
        connectionHandler.start();
    }

}
