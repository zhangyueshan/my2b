package wings.my2b.io;

import wings.my2b.exception.My2bException;
import wings.my2b.exception.UnsupportedProtocolVersionException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by InThEnd on 2016/9/1.
 * My2bIO
 */
public class My2bIO {

    private Socket conn;

    private int protocolVersion;

    private String serverVersion;

    //server pId;
    private int pId;

    //challenge seed
    private String seed;


    public My2bIO(Socket conn) {
        this.conn = conn;
    }


    public void doHandShake() throws IOException {
        InputStream is = conn.getInputStream();
        byte[] packetHeaderBuf = new byte[4];
        readFully(is, packetHeaderBuf, 0, 4);
        int packetLength = (packetHeaderBuf[0] & 0xff) + ((packetHeaderBuf[1] & 0xff) << 8) + ((packetHeaderBuf[2] & 0xff) << 16);
        byte[] buf = new byte[packetLength];
        readFully(is, buf, 0, packetLength);
        Packet packet = new Packet(buf);
        protocolVersion = packet.readByte();
        if (protocolVersion != 10) {
            throw new UnsupportedProtocolVersionException(protocolVersion);
        }
        serverVersion = packet.readNullEndString("ASCII");

    }

    private static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }

        int n = 0;

        while (n < len) {
            int count = in.read(b, off + n, len - n);

            if (count < 0) {
                throw new My2bException("...");
            }

            n += count;
        }

        return n;
    }

    private static long skipFully(InputStream in, long len) throws IOException {
        if (len < 0) {
            throw new IOException("Negative skip length not allowed");
        }

        long n = 0;

        while (n < len) {
            long count = in.skip(len - n);

            if (count < 0) {
                throw new My2bException("...");
            }

            n += count;
        }

        return n;
    }

}
