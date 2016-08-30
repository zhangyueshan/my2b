package wings.my2b.io;

import wings.my2b.exception.My2bException;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * Created by InThEnd on 2016/8/29.
 * ToBeMain
 */
public class ToBeMain {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("192.168.2.9", 3306);
        InputStream is = socket.getInputStream();
        byte[] packetHeaderBuf = new byte[4];
        readFully(is, packetHeaderBuf, 0, 4);
        int packetLength = (packetHeaderBuf[0] & 0xff) + ((packetHeaderBuf[1] & 0xff) << 8) + ((packetHeaderBuf[2] & 0xff) << 16);
        System.out.println("包长度：" + packetLength);
        byte[] buf = new byte[packetLength];
        readFully(is, buf, 0, packetLength);
        Packet packet = new Packet(buf);
        System.out.println("协议版本号：" + packet.readByte());
        System.out.println("服务器版本号：" + packet.readNullEndString("ASCII"));
        System.out.println("服务器线程ID：" + packet.read4Int());
        System.out.println("挑战随机数1：" + packet.read8Int());
        System.out.println("填充：" + packet.readByte());
        packet.jump(2);
        packet.jump(1);
        packet.jump(2);
        packet.jump(2);
        packet.jump(1);
        byte[] bytes = packet.readBytes(10);

        System.out.println("挑战随机数2：" + packet.read8Int());
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
