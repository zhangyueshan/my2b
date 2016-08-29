package wings.my2b.io;

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
        byte[] buf = new byte[1024];
        is.read(buf);
    }
}
