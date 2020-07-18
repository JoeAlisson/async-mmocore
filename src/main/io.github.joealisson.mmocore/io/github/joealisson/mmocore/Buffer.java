package io.github.joealisson.mmocore;

/**
 * @author JoeAlisson
 */
public interface Buffer {

    byte readByte(int index);

    void writeByte(int index, byte value);

    short readShort(int index);

    /**
     * Write <B>int</B> to the buffer at index. <BR>
     * 16bit integer (00 00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeShort(int index, short value);

    int readInt(int index);

    void writeInt(int index, int value);

    int limit();

    void limit(int newLimit);

}
