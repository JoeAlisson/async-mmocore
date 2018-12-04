# async-mmocore
![Code Badge](https://img.shields.io/badge/Project-L2J-red.svg)
![Code Badge](https://img.shields.io/badge/Powered_by-Java_1.8-lightgray.svg)
[![License badge](https://img.shields.io/badge/license-GPL-blue.svg)](https://opensource.org/licenses/AGPL-3.0)
[![Code Badge](https://img.shields.io/badge/Versioning-Semantic-green.svg)](https://semver.org/)
[![Build Status](https://travis-ci.org/JoeAlisson/async-mmocore.svg?branch=master)](https://travis-ci.org/JoeAlisson/async-mmocore)

#### The  Goal

The _**Async-mmocore**_ is primary designed to **Massive Multiplayer Online (MMO) Game Servers**. 
The Goal of the Async-mmocore is to provide a easy way to handle MMO connections to a server abstracting the networking layer complexity.

#### The Requirements 

The _**Async-mmocore**_ is built on top of [Java NIO.2 API](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/AsynchronousSocketChannel.html) using Asyncronous Socket Channels. It requires Java 8+ to build and run.

###### The ReadablePacket and WritablePacket Classes

These classes, herein referenced as **packets**, are the abstraction of data send through the network.
All packets must have a **Header** and a optional **payload**. 

The header is composed by a **Short** number the carries out the size the packet. 
The payload is the essential information to the server or client. The packet must be composed by at maximum 32767 bytes.
Packets greater than this can be lead to unexpected behaviour. 

#### The Basics to Use

* ##### Define a Client Implementation

The client Class is a representation of a external connection. Thus it's the unique source of incoming packets and the target of the outcome packets.

The Client Class must implement the [abstract class Client](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/org.l2j.mmocore/org/l2j/mmocore/Client.java) 

```java
public class ClientImpl extends Client<Connection<ClientImpl>> {
    
    public ClientImpl(Connection<ClientImpl> connection) {
        super(connection);
    }
        
    @Override
    public boolean decrypt(byte[] data, int offset, int size) {
        return myCrypter.decrypt(data, offset, size);
    }
    
    @Override
    public int encrypt(byte[] data, int offset, int size) {
        return myCrypter.encrypt(data, offset, size);
    }
    
    @Override
    protected void onDisconnection() {
        saveDataAndReleaseResources();
    }
    
    @Override
    public void onConnected() {
        doTheInitialJob();    
    }
    
    public void sendPacket(WritablePacket<ClientImpl> packet) {
        writePacket(packet);
    }
}
```

* ##### Define a Client Factory Implementation

The Client Factory instantiate the new incoming connections. 

The Client Factory must implement the [interface ClientFactory](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/org.l2j.mmocore/org/l2j/mmocore/ClientFactory.java)

```java
public class ClientFactoryImpl implements ClientFactory<ClientImpl> {
    
    @Override
    public ClientImpl create(Connection<ClientImpl> connection) {
        return new ClientImpl(connection);
    }    
}
``` 

* ##### Define a Packet Handler Implementation

The Packet Handler converts the incoming data into a **ReadablePacket**.

The Packet Handler must implement the [interface PacketHandler](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/org.l2j.mmocore/org/l2j/mmocore/PacketHandler.java)
```java
public class PacketHandlerImpl implements PacketHandler<ClientImpl> {
    
     @Override
    public ReadablePacket<ClientImpl> handlePacket(DataWrapper wrapper, ClientImpl client) {
        ReadablePacket<ClientImpl> packet = convertToPacket(wrapper, client);
        return packet;
    }
}

```

* ##### Define a Packet Executor Implementation

The Packet Executor executes the incoming Packets. 

**Although the packet can be execute in the same Thread, it's highly recommended that the Executors executes the packet on a apart Thread.
Thats because the Thread that calls the _execute_ method is the same that process the network I/O operations. Thus these threads must be short-living and execute only no-blocking operations.**

The Packet Executor must implement the [interface PacketExecutor](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/org.l2j.mmocore/org/l2j/mmocore/PacketExecutor.java)

```java
public class PacketExecutorImpl implements PacketExecutor<ClientImpl> {
    
    @Override
    public void execute(ReadablePacket<AsyncClient> packet) { 
        threadPoolExecutor.execute(packet);
    }
}
```  

* ##### Listen Connections

To listen Connections it's necessary to build a ConnectionHandler

```java
public class ServerHandler {
    public void startListen(String host, int port) { 
        ConnectionHandler<ClientImpl> connectionHandler = ConnectionBuilder.create(new InetSocketAddress(host, port), new ClientFactoryImpl(), new PacketHandlerImpl(), new PacketExecutorImpl()).build();
        connectionHandler.start();
    }    
} 

```

* ##### Sending a Packet

To send a Packet it's necessary to implement the [abstract class WritablePacket](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/org.l2j.mmocore/org/l2j/mmocore/WritablePacket.java)

```java
public class ServerInfo implements WritablePacket<ClientImpl> {
    @Override
    protected void write() {
        writeByte(this.getServerId());
        writeString(this.getServerName());
        writeLong(this.getServerCurrentTime());
        writeInt(this.getServerCurrentUsers());
        
    }
}
```
and just send it through the client

```java
public class ServerHandler {
    public void sendServerInfoToClient(ClientImpl client) {
        client.sendPacket(new ServerInfo());
    }
}
```

* ##### Receiving a Packet

The receiving packet is almost all done by the **Async-mmocore**. The only part that needs to be implemented to fully read are the steps described in [Define a Packet Handler Implementation](#define-a-packet-handler-implementation) and [Define a Packet Executor Implementation](#define-a-packet-executor-implementation) sections.  
