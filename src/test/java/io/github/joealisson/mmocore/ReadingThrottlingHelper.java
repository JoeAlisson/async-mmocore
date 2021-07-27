package io.github.joealisson.mmocore;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

class ReadingThrottlingHelper {

    static RTClient lastClient;

    public static  RTClient create(Connection<RTClient> connection) {
        lastClient = new RTClient(connection);
        return lastClient;
    }


    public static void execute(ReadablePacket<RTClient> packet) {
        packet.run();
    }

    static class RTClient extends Client<Connection<RTClient>> {

        public Instant pingReceived;
        public Instant ping2ndReceived;
        public AtomicBoolean readableAgain = new AtomicBoolean(false);

        protected RTClient(Connection<RTClient> connection) {
            super(connection);
        }

        public boolean hasMinimumTimeBetweenPackets() {
            return ChronoUnit.SECONDS.between(pingReceived, ping2ndReceived) >= 2;
        }

        @Override
        public boolean encrypt(Buffer data, int offset, int size) {
            return true;
        }

        @Override
        public boolean decrypt(Buffer data, int offset, int size) {
            return true;
        }

        @Override
        protected void onDisconnection() {

        }

        @Override
        public void onConnected() {

        }

    }

    public static WritablePacket<RTClient> ping2nd() {
        return new WritablePacket<>() {
            @Override
            protected boolean write(RTClient client, WritableBuffer buffer) {
                buffer.writeByte(2);
                buffer.writeString("PING 2ND");
                return true;
            }
        };
    }

    public static WritablePacket<RTClient> ping() {
        return new WritablePacket<>() {
            @Override
            protected boolean write(RTClient client, WritableBuffer buffer) {
                buffer.writeByte(1);
                buffer.writeString("PING");
                return true;
            }
        };
    }

    public static ReadablePacket<RTClient> handlePacket(ReadableBuffer readableBuffer, RTClient client) {
        byte opcode = readableBuffer.readByte();
        if(opcode == 1) {
            return readablePing();
        } else if (opcode == 2) {
            return readablePing2nd();
        }
        return null;
    }

    private static ReadablePacket<RTClient> readablePing2nd() {
        return new ReadablePacket<>() {
            @Override
            protected boolean read() {
                return true;
            }

            @Override
            public void run() {
                client.ping2ndReceived = Instant.now();
                client.readableAgain.set(true);
            }
        };
    }

    private static ReadablePacket<RTClient> readablePing() {
        return new ReadablePacket<>() {
            @Override
            protected boolean read() {
                return true;
            }

            @Override
            public void run() {
                client.pingReceived = Instant.now();
                CompletableFuture.delayedExecutor(2500, TimeUnit.MILLISECONDS).execute(client::readNextPacket);
            }
        };
    }

}
