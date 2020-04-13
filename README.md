# async-mmocore
![Code Badge](https://img.shields.io/badge/Project-L2J-red.svg?logo=github&logoColor=white)
![Code Badge](https://img.shields.io/badge/Powered_by-Java_11-lightgray.svg?logo=java&logoColor=white)
[![Code Badge](https://img.shields.io/badge/Versioning-Semantic-green.svg?logo=git&logoColor=white)](https://semver.org/)
[![License badge](https://img.shields.io/badge/license-GPL-blue.svg?logo=gnu&logoColor=white)](https://opensource.org/licenses/AGPL-3.0)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.joealisson/async-mmocore/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.joealisson/async-mmocore)

###### Quality Metrics

[![Build Status](https://travis-ci.org/JoeAlisson/async-mmocore.svg?branch=master)](https://travis-ci.org/JoeAlisson/async-mmocore)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=alert_status)](https://sonarcloud.io/dashboard?id=JoeAlisson_async-mmocore)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=JoeAlisson_async-mmocore)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=security_rating)](https://sonarcloud.io/dashboard?id=JoeAlisson_async-mmocore)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=JoeAlisson_async-mmocore)
[![SonarCloud Coverage](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=coverage)](https://sonarcloud.io/component_measures?id=JoeAlisson_async-mmocore&metric=Coverage)
[![SonarCloud Bugs](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=bugs)](https://sonarcloud.io/component_measures/metric/reliability_rating/list?id=JoeAlisson_async-mmocore)
[![SonarCloud Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=JoeAlisson_async-mmocore&metric=vulnerabilities)](https://sonarcloud.io/component_measures/metric/security_rating/list?id=JoeAlisson_async-mmocore)


[Para mais detalhes, participe da discussão sobre o assunto no fórum L2jBrasil](https://www.l2jbrasil.com/index.php?/topic/130598-mmocore-20-ass%C3%ADncrono/)

#### The  Goal

The _**Async-mmocore**_ is primary designed to **Massive Multiplayer Online (MMO) Game Servers**. 
The Goal of the Async-mmocore is to provide an easy way to handle MMO connections to a server abstracting the networking layer complexity.

#### The Requirements 

The _**Async-mmocore**_ is built on top of [Java NIO.2 API](https://openjdk.java.net/projects/nio/) using Asynchronous Socket Channels. It is recommended Java 11+ to build and run.

###### The ReadablePacket and WritablePacket Classes

These classes, herein referenced as **packets**, are the abstraction of data send through the network.
All packets must have a **Header** and an optional **payload**. 

The header is a **Short** number the carries out the size the packet. 
The payload is the essential information to the server or client. The packet must be composed by at maximum 32767 bytes.
Packets greater than this can be lead to unexpected behaviour. 

#### The Basics to Use

* ##### Define a Client Implementation

The client Class is a representation of an external connection. Thus, it's the unique source of incoming packets and the target of the outcome packets.

The Client Class must implement the [abstract class Client](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/io.github.joealisson.mmocore/io/github/joealisson/mmocore/Client.java) 

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
    public int encryptedSize(int dataSize) {
        return myCrypter.calcEncryptedSize(dataSize);
    }
    
    @Override
    public int[] encrypt(byte[] data, int offset, int size) {
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

The Client Factory must implement the [interface ClientFactory](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/io.github.joealisson.mmocore/io/github/joealisson/mmocore/ClientFactory.java)

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

The Packet Handler must implement the [interface PacketHandler](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/io.github.joealisson.mmocore/io/github/joealisson/mmocore/PacketHandler.java)
```java
public class PacketHandlerImpl implements PacketHandler<ClientImpl> {
    
     @Override
    public ReadablePacket<ClientImpl> handlePacket(PacketBuffer buffer, ClientImpl client) {
        ReadablePacket<ClientImpl> packet = convertToPacket(buffer, client);
        return packet;
    }
}

```

* ##### Define a Packet Executor Implementation

The Packet Executor executes the incoming Packets. 

**Although the packet can be executed in the same Thread, it's HIGHLY recommended that the Executors executes the packet on an apart Thread.
That's because the Thread that calls the _execute_ method is the same which process the network I/O operations. Thus, these threads must be short-living and execute only non-blocking operations.**

The Packet Executor must implement the [interface PacketExecutor](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/io.github.joealisson.mmocore/io/github/joealisson/mmocore/PacketExecutor.java)

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

To send a Packet it's necessary to implement the [abstract class WritablePacket](https://github.com/JoeAlisson/async-mmocore/blob/master/src/main/io.github.joealisson.mmocore/io/github/joealisson/mmocore/WritablePacket.java)

```java
public class ServerInfo implements WritablePacket<ClientImpl> {
    @Override
    protected boolean write(ClientImpl client, ByteBuffer buffer) {
        writeByte(this.getServerId());
        writeString(this.getServerName());
        writeLong(this.getServerCurrentTime());
        writeInt(this.getServerCurrentUsers());
        return true;      
    }
}
```
After it just send it through the client

```java
public class ServerHandler {
    public void sendServerInfoToClient(ClientImpl client) {
        client.sendPacket(new ServerInfo());
    }
}
```

* ##### Receiving a Packet

The receiving packet is almost all done by the **Async-mmocore**. The only part to be implemented to fully read is the steps described in [Define a Packet Handler Implementation](#define-a-packet-handler-implementation) and [Define a Packet Executor Implementation](#define-a-packet-executor-implementation) sections.  
```java
public class ReceivedServerInfo implements ReadablePacket<ClientImpl> {
    
    @Override
    protected boolean read(ByteBuffer buffer) {
        this.serverId = buffer.get();
        this.serverName = readString(buffer);
        this.serverCurrentTime = buffer.getLong();
        this.serverCurrentUsers = buffer.getInt();
        return true;
    }
    
    @Override
    public void run() {
        showServerInfoToClient();
    }
}
```

#### Client Side

The class Connector provides client side asynchronous connection support. It works just like ConnectionBuilder, so you must define the ClientFactory, the PacketHandler and the PacketExecutor implementations.

```java
public class ConnectionFactory {

    public static ClientImpl create(String host, int port) {
        ClientImpl client = Connector.create(clientFactory, packetHandler, packetExecutor).connect(new InetSocketAddress(host, port));
        return client;
    }

}
```
