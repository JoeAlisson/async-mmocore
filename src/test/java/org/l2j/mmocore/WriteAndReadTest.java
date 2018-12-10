package org.l2j.mmocore;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class WriteAndReadTest {

    ReceivablePacket receivablePacket;
    @Before
    public void setUp() {
        receivablePacket = new ReceivablePacket();
    }

    @Test
    public void testWriteByte() {

        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeByte(1);
                writeByte((byte) 2);
                writeByte(true);
                writeByte(false);
                writeByte(Byte.MAX_VALUE);
                writeByte(Byte.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(8, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(6, receivablePacket.readShort());
        assertEquals(1, receivablePacket.readByte());
        assertEquals(2, receivablePacket.readByte());
        assertEquals(1, receivablePacket.readByte());
        assertEquals(0, receivablePacket.readByte());
        assertEquals(Byte.MAX_VALUE, receivablePacket.readByte());
        assertEquals(Byte.MIN_VALUE, receivablePacket.readByte());
    }

    @Test
    public void writeDoubleTest() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeDouble(0.5);
                writeDouble(1.5);
                writeDouble(-1.5);
                writeDouble(Double.MAX_VALUE);
                writeDouble(Double.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(42, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(40, receivablePacket.readShort());
        assertEquals(0.5, receivablePacket.readDouble(), 0);
        assertEquals(1.5, receivablePacket.readDouble(), 0);
        assertEquals(-1.5, receivablePacket.readDouble(), 0);
        assertEquals(Double.MAX_VALUE, receivablePacket.readDouble(), 0);
        assertEquals(Double.MIN_VALUE, receivablePacket.readDouble(), 0);
    }

    @Test
    public void testWriteShort() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeShort(0);
                writeShort(1);
                writeShort(-1);
                writeShort(true);
                writeShort(false);
                writeShort(Short.MAX_VALUE);
                writeShort(Short.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(16, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(14, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readShort());
        assertEquals(1, receivablePacket.readShort());
        assertEquals(-1, receivablePacket.readShort());
        assertEquals(1, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readShort());
        assertEquals(Short.MAX_VALUE, receivablePacket.readShort());
        assertEquals(Short.MIN_VALUE, receivablePacket.readShort());

    }

    @Test
    public void testWriteInt() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeInt(0);
                writeInt(1);
                writeInt(-1);
                writeInt(true);
                writeInt(false);
                writeInt(Integer.MAX_VALUE);
                writeInt(Integer.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(30, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(28, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(-1, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(Integer.MAX_VALUE, receivablePacket.readInt());
        assertEquals(Integer.MIN_VALUE, receivablePacket.readInt());
    }

    @Test
    public void testWriteFloat() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeFloat(0.5f);
                writeFloat(1.5f);
                writeFloat(-1.5f);
                writeFloat(Float.MAX_VALUE);
                writeFloat(Float.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(22, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(20, receivablePacket.readShort());
        assertEquals(0.5f, receivablePacket.readFloat(), 0);
        assertEquals(1.5, receivablePacket.readFloat(),0);
        assertEquals(-1.5, receivablePacket.readFloat(), 0);
        assertEquals(Float.MAX_VALUE, receivablePacket.readFloat(), 0);
        assertEquals(Float.MIN_VALUE, receivablePacket.readFloat(), 0);
    }

    @Test
    public void testWriteLong() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeLong(0);
                writeLong(1);
                writeLong(-1);
                writeLong(Long.MAX_VALUE);
                writeLong(Long.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(42, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(40, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readLong());
        assertEquals(1, receivablePacket.readLong());
        assertEquals(-1, receivablePacket.readLong());
        assertEquals(Long.MAX_VALUE, receivablePacket.readLong());
        assertEquals(Long.MIN_VALUE, receivablePacket.readLong());
    }

    @Test
    public void testWriteBytes() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                byte[] source = new byte[] { 0, 1, -1, Byte.MAX_VALUE, Byte.MIN_VALUE };
                writeBytes(source);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(7, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(5, receivablePacket.readShort());

        byte[] bytes = new byte[5];
        receivablePacket.readBytes(bytes);

        assertEquals(0, bytes[0]);
        assertEquals(1, bytes[1]);
        assertEquals(-1, bytes[2]);
        assertEquals(Byte.MAX_VALUE, bytes[3]);
        assertEquals(Byte.MIN_VALUE, bytes[4]);
    }

    @Test
    public void testWriteChar() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeChar((char) 0);
                writeChar((char) 1);
                writeChar((char) -1);
                writeChar(Character.MAX_VALUE);
                writeChar(Character.MIN_VALUE);
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(12, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(10, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readChar());
        assertEquals(1, receivablePacket.readChar());
        assertEquals((char)-1, receivablePacket.readChar());
        assertEquals(Character.MAX_VALUE, receivablePacket.readChar());
        assertEquals(Character.MIN_VALUE, receivablePacket.readChar());
    }


    @Test
    public void testWriteString() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeString("");
                writeString("anyString");
                writeString("String#With$Special-Characters%");
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(88, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(86, receivablePacket.readShort());
        assertEquals("", receivablePacket.readString());
        assertEquals("anyString", receivablePacket.readString());
        assertEquals("String#With$Special-Characters%", receivablePacket.readString());
    }


    @Test
    public void testWriteSizedString() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeSizedString("");
                writeSizedString("anyString");
                writeSizedString("String#With$Special-Characters%");
                return true;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(88, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(86, receivablePacket.readShort());
        assertEquals("", receivablePacket.readSizedString());
        assertEquals("anyString", receivablePacket.readSizedString());
        assertEquals("String#With$Special-Characters%", receivablePacket.readSizedString());
    }


    @Test
    public void testWriteOnGrowingData() {
        WritablePacket<AsyncClient> sendablePacket = new WritablePacket<AsyncClient>() {
            @Override
            protected boolean write() {
                writeInt(0);
                writeInt(1);
                writeInt(-1);
                writeInt(true);
                writeInt(false);
                writeInt(Integer.MAX_VALUE);
                writeInt(Integer.MIN_VALUE);
                return true;
            }

            @Override
            protected int packetSize() {
                return 2;
            }
        };

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(30, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(28, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(-1, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(Integer.MAX_VALUE, receivablePacket.readInt());
        assertEquals(Integer.MIN_VALUE, receivablePacket.readInt());
    }


    @Test
    public void testWriteStaticData() {

        @StaticPacket
        class StaticDataPacket extends WritablePacket<AsyncClient> {

            @Override
            protected boolean write() {
                writeInt(0);
                writeInt(1);
                writeInt(-1);
                writeInt(true);
                writeInt(false);
                writeInt(Integer.MAX_VALUE);
                writeInt(Integer.MIN_VALUE);
                return true;
            }
        }

        WritablePacket<AsyncClient> sendablePacket = new StaticDataPacket();

        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(30, sendablePacket.dataIndex);

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(28, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(-1, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(Integer.MAX_VALUE, receivablePacket.readInt());
        assertEquals(Integer.MIN_VALUE, receivablePacket.readInt());


        sendablePacket.writeData();
        sendablePacket.writeHeader(sendablePacket.dataIndex - ReadHandler.HEADER_SIZE);

        assertEquals(30, sendablePacket.dataIndex);

        receivablePacket = new ReceivablePacket();

        receivablePacket.data = Arrays.copyOf(sendablePacket.data, sendablePacket.dataIndex);

        assertEquals(28, receivablePacket.readShort());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(-1, receivablePacket.readInt());
        assertEquals(1, receivablePacket.readInt());
        assertEquals(0, receivablePacket.readInt());
        assertEquals(Integer.MAX_VALUE, receivablePacket.readInt());
        assertEquals(Integer.MIN_VALUE, receivablePacket.readInt());
    }

}
