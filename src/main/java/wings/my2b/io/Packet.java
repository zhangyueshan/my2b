package wings.my2b.io;

import java.net.Socket;

/**
 * Created by InThEnd on 2016/8/29.
 * Data packet.
 */
public class Packet {

    private byte[] buf = new byte[0xFFFFFF];

    //packet length
    private int len;

    //private constructor
    private Packet() {
    }

    public static Packet getPacket(final Socket socket) {
        return null;
    }


}
