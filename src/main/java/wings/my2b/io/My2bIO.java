package wings.my2b.io;

import com.mysql.jdbc.*;
import wings.my2b.exception.My2bException;
import wings.my2b.exception.UnsupportedProtocolVersionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.sql.SQLException;
import java.util.zip.Deflater;

/**
 * Created by InThEnd on 2016/9/1.
 * My2bIO
 */
public class My2bIO {

    protected static final int HEADER_LENGTH = 4;
    protected static final int AUTH_411_OVERHEAD = 33;

    protected static final int COMP_HEADER_LENGTH = 3;

    protected static final int MIN_COMPRESS_LEN = 50;


    private static final int CLIENT_LONG_PASSWORD = 0x00000001; /* new more secure passwords */
    private static final int CLIENT_FOUND_ROWS = 0x00000002;
    private static final int CLIENT_LONG_FLAG = 0x00000004; /* Get all column flags */
    protected static final int CLIENT_CONNECT_WITH_DB = 0x00000008;
    private static final int CLIENT_COMPRESS = 0x00000020; /* Can use compression protcol */
    private static final int CLIENT_LOCAL_FILES = 0x00000080; /* Can use LOAD DATA LOCAL */
    private static final int CLIENT_PROTOCOL_41 = 0x00000200; // for > 4.1.1
    private static final int CLIENT_INTERACTIVE = 0x00000400;
    protected static final int CLIENT_SSL = 0x00000800;
    private static final int CLIENT_TRANSACTIONS = 0x00002000; // Client knows about transactions
    protected static final int CLIENT_RESERVED = 0x00004000; // for 4.1.0 only
    protected static final int CLIENT_SECURE_CONNECTION = 0x00008000;
    private static final int CLIENT_MULTI_STATEMENTS = 0x00010000; // Enable/disable multiquery support
    private static final int CLIENT_MULTI_RESULTS = 0x00020000; // Enable/disable multi-results
    private static final int CLIENT_PLUGIN_AUTH = 0x00080000;
    private static final int CLIENT_CONNECT_ATTRS = 0x00100000;
    private static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 0x00200000;
    private static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORD = 0x00400000;
    private static final int CLIENT_SESSION_TRACK = 0x00800000;
    private static final int CLIENT_DEPRECATE_EOF = 0x01000000;


    private Socket conn;

    private int protocolVersion;

    private String serverVersion;

    //server pId;
    private long pId;

    //challenge seed
    private String seed;

    private int serverCapabilities;

    private int serverCharsetIndex;

    private int serverStatus;

    private int maxAllowedPacket;

    //max packet len
    private int maxThreeBytes = (256 * 256 * 256) - 1;

    private boolean useNewLargePackets = true;

    private int serverMajorVersion = 0;
    private int serverMinorVersion = 0;
    private int serverSubMinorVersion = 0;

    private boolean useCompression = false;

    private byte packetSequence = 0;
    private byte compressedPacketSequence = 0;

    private Deflater deflater = null;

    private InputStream is;

    private OutputStream os;

    public My2bIO(Socket conn) throws IOException {
        this.conn = conn;
        is = conn.getInputStream();
        os = conn.getOutputStream();

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
        pId = packet.read4Int();
        seed = packet.readLengthExpectedString("ASCII", 8);
        packet.readByte();

        // read capability flags (lower 2 bytes)
        if (packet.getPosition() < packet.getBufLength()) {
            serverCapabilities = packet.read2Int();
        }

        //charset
        serverCharsetIndex = packet.readByte() & 0xff;

        serverStatus = packet.read2Int();

        //capability part2
        serverCapabilities |= packet.read2Int() << 16;

        //10 byte 0x00 fill
        packet.jump(10);

        String seedPart2;
        StringBuilder newSeed;
        seedPart2 = packet.readLengthExpectedString("ASCII", 12);
        newSeed = new StringBuilder();
        newSeed.append(this.seed);
        newSeed.append(seedPart2);
        this.seed = newSeed.toString();

        String user = "root";
        String database = "ucoin";
        int passwordLength = 16;
        int userLength = (user != null) ? user.length() : 0;
        int databaseLength = (database != null) ? database.length() : 0;

        int packLength = ((userLength + passwordLength + databaseLength) * 3) + 7 + HEADER_LENGTH + AUTH_411_OVERHEAD;
        Packet toServer = new Packet(new byte[packetLength]);
        System.out.println(packLength);


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

    /**
     * @param packet
     * @param packetLen length of header + payload
     * @throws SQLException
     */
    private final void send(Packet packet, int packetLen) throws SQLException {
        try {
            if (this.maxAllowedPacket > 0 && packetLen > this.maxAllowedPacket) {
                throw new PacketTooBigException(packetLen, this.maxAllowedPacket);
            }

            if ((this.serverMajorVersion >= 4) && (packetLen - HEADER_LENGTH >= this.maxThreeBytes
                    || (this.useCompression && packetLen - HEADER_LENGTH >= this.maxThreeBytes - COMP_HEADER_LENGTH))) {
                //sendSplitPackets(packet, packetLen);

            } else {
                this.packetSequence++;

                Packet packetToSend = packet;
                packetToSend.setPosition(0);
                packetToSend.write3Int(packetLen - HEADER_LENGTH);
                packetToSend.writeByte(this.packetSequence);

                if (this.useCompression) {
                    //ignore now
                }

                os.write(packetToSend.getByteBuffer(), 0, packetLen);
                os.flush();
            }

        } catch (IOException ioEx) {
            throw new My2bException("IO异常。");
        }
    }


}
