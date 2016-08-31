package wings.my2b.io;

import wings.my2b.StringUtils;
import wings.my2b.exception.My2bException;

import java.io.UnsupportedEncodingException;

/**
 * Created by InThEnd on 2016/8/29.
 * Data packet.
 */
public class Packet {

    //data
    private byte[] byteBuffer;

    //packet length
    private int bufLength;

    //read position
    private int position = 0;

    //constructor
    public Packet(byte[] buf) {
        this.byteBuffer = buf;
        this.bufLength = buf.length;
    }

    public byte[] getByteBuffer() {
        return byteBuffer;
    }

    public int getBufLength() {
        return bufLength;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    final byte readByte() {
        return this.byteBuffer[this.position++];
    }

    final byte readByte(int readAt) {
        return this.byteBuffer[readAt];
    }


    /**
     * Read string[NUL]
     */
    String readNullEndString(String encoding) {
        int i = this.position;
        int len = 0;
        int maxLen = getBufLength();

        while ((i < maxLen) && (this.byteBuffer[i] != 0)) {
            len++;
            i++;
        }
        try {
            return StringUtils.toString(this.byteBuffer, this.position, len, encoding);
        } catch (UnsupportedEncodingException uEE) {
            throw new My2bException(uEE);
        } finally {
            this.position += (len + 1); // update cursor
        }
    }

    /**
     * Read string[$len]
     */
    String readLengthExpectedString(String encoding, int expectedLength) {
        if (this.position + expectedLength > getBufLength()) {
            throw new My2bException("长度不够。");
        }
        try {
            return StringUtils.toString(this.byteBuffer, this.position, expectedLength, encoding);
        } catch (UnsupportedEncodingException uEE) {
            throw new My2bException(uEE);
        } finally {
            this.position += expectedLength; // update cursor
        }
    }

    int read2Int() {
        byte[] b = this.byteBuffer; // a little bit optimization
        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8);
    }

    int read3Int() {
        byte[] b = this.byteBuffer;
        return (b[this.position++] & 0xff) | ((b[this.position++] & 0xff) << 8) | ((b[this.position++] & 0xff) << 16);
    }

    long read4Int() {
        byte[] b = this.byteBuffer;
        return ((long) b[this.position++] & 0xff) | (((long) b[this.position++] & 0xff) << 8) | ((long) (b[this.position++] & 0xff) << 16)
                | ((long) (b[this.position++] & 0xff) << 24);
    }

    long read8Int() {
        byte[] b = this.byteBuffer;
        return (b[this.position++] & 0xff) | ((long) (b[this.position++] & 0xff) << 8) | ((long) (b[this.position++] & 0xff) << 16)
                | ((long) (b[this.position++] & 0xff) << 24) | ((long) (b[this.position++] & 0xff) << 32) | ((long) (b[this.position++] & 0xff) << 40)
                | ((long) (b[this.position++] & 0xff) << 48) | ((long) (b[this.position++] & 0xff) << 56);
    }

    void jump(int len) {
        position += len;
    }


    byte[] readBytes(int len) {
        byte[] dest = new byte[len];
        System.arraycopy(this.byteBuffer, position, dest, 0, len);
        position += len;
        return dest;
    }


}
