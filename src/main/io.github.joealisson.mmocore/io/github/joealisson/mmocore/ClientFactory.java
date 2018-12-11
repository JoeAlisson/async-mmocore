package io.github.joealisson.mmocore;

public interface ClientFactory<T extends Client<Connection<T>>> {

    /**
     * This method must create a Client using the connection parameter.
     *
     * @param connection - the underlying connection to client.
     *
     * @return a client implementation.
     */
    T create(Connection<T> connection);
}
