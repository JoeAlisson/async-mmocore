package io.github.joealisson.mmocore;

/**
 * @author JoeAlisson
 */
public interface Buffer {

    /**
     * Read <B>byte</B> from the buffer at index. <BR>
     * 8bit integer (00)
     *
     * @param index index to read from
     * @return the byte value at the index.
     */
    byte readByte(int index);

    /**
     * Write <B>byte</B> to the buffer at index. <BR>
     * 8bit integer (00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeByte(int index, byte value);

    /**
     * Read <B>short</B> from the buffer at index. <BR>
     * 16bit integer (00 00)
     *
     * @param index index to read from
     * @return the short value at index.
     */
    short readShort(int index);

    /**
     * Write <B>int</B> to the buffer at index. <BR>
     * 16bit integer (00 00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeShort(int index, short value);

    /**
     * Read <B>int</B> from the buffer at index. <BR>
     * 32bit integer (00 00 00 00)
     *
     * @param index index to read from
     * @return the int value at index.
     */
    int readInt(int index);

    /**
     * Write <B>int</B> to the buffer at index. <BR>
     * 32bit integer (00 00 00 00)
     *
     * @param index index to write to
     * @param value to be written
     */
    void writeInt(int index, int value);

    /**
     * @return the buffer's limit
     */
    int limit();

    /**
     * Change the buffer's limit
     *
     * @param newLimit the new limit
     */
    void limit(int newLimit);

}
