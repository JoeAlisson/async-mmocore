package org.l2j.mmocore;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.isNull;

public abstract class Client<T extends Connection<?>> {

    private final T connection;
    private Queue<WritablePacket<? extends Client<T>>> packetsToWrite = new ConcurrentLinkedQueue<>();
    private int dataSentSize;
    private AtomicBoolean writing = new AtomicBoolean(false);

    public Client(T connection) {
        this.connection = connection;
    }

    T getConnection() {
        return connection;
    }

    protected void writePacket(WritablePacket<? extends Client<T>> packet) {
        putClientOnPacket(packet);
        if(packetsToWrite.isEmpty() && writing.compareAndSet(false, true) ) {
            write(packet);
        } else {
            packetsToWrite.add(packet);
        }
    }

    @SuppressWarnings("unchecked")
    private void putClientOnPacket(WritablePacket packet) {
        packet.client = this;
    }

    void tryWriteNextPacket() {
        if(packetsToWrite.isEmpty()) {
            writing.getAndSet(false);
        } else {
            WritablePacket<? extends Client<T>> packet = packetsToWrite.poll();
            write(packet);
        }
    }

    void resumeSend(int result) {
        dataSentSize-= result;
        connection.write();
    }


    private void write(WritablePacket packet, boolean sync) {
        if(isNull(packet)) {
            return;
        }

        int dataSize = packet.writeData();
        dataSentSize  = encrypt(packet.data, ReadHandler.HEADER_SIZE, dataSize - ReadHandler.HEADER_SIZE) + ReadHandler.HEADER_SIZE;
        packet.writeHeader(dataSentSize);
        if(dataSentSize > 0) {
            connection.write(packet.data, 0, dataSentSize, sync);
        }
    }

    private void write(WritablePacket<? extends Client<T>> packet) {
        write(packet, false);
    }

    public void close(WritablePacket<? extends Client<T>> packet) {
        packetsToWrite.clear();
        putClientOnPacket(packet);
        write(packet, true);
    }

    protected final void disconnect() {
        connection.close();
        onDisconnection();
    }

    int getDataSentSize() {
        return dataSentSize;
    }

    public String getHostAddress() {
        return connection.getRemoteAddress();
    }

    public boolean isConnected() {
        return connection.isOpen();
    }

    /**
     * @param data - the data to be encrypted
     * @param offset - the initial index to be encrypted
     * @param size - the length of data to be encrypted
     * @return The size of the data encrypted
     */
    public abstract int encrypt(byte[] data, int offset, int size);
    public abstract boolean decrypt(byte[] data, int offset, int size);
    protected abstract void  onDisconnection();
    public abstract void onConnected();
}
