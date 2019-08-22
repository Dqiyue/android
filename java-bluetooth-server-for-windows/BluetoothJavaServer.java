import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
 
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
 

public class BluetoothJavaServer {
    // 蓝牙服务器端的UUID必须和手机端的UUID一致。
    // 手机端的UUID需要去掉中间的-分割符。
    private String PC_UUID;
 
    public static void main(String[] args) {
        new BluetoothJavaServer();
    }

    //确保dst长度足够此处不做判断
    public static void writeInt32(byte[] dst, int off, int v) {
        dst[off] = (byte)(v >> 24 & 0xff);
        dst[off + 1] = (byte)(v >> 16 & 0xff);
        dst[off + 2] = (byte)(v >> 8 & 0xff);
        dst[off + 3] = (byte)(v & 0xff);
    }

    public static int readInt32(byte[] src, int off) {
        int ret = ((int)src[off] & 0xFF) << 24;
        ret += ((int)src[off + 1] & 0xFF) << 16;
        ret += ((int)src[off + 2] & 0xFF) << 8;
        ret += ((int) src[off + 3] & 0xFF);
        return ret;
    }

    public static void writeDouble(byte[] dst,int off,double d) {
        long value = Double.doubleToRawLongBits(d);
        for (int i = 0; i < 8; i++) {
            dst[off + i] = (byte) ((value >> 8 * i) & 0xff);
        }
    }

    public static double readDouble(byte[] src,int off) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (src[off + i] & 0xFF)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }
 
    public BluetoothJavaServer() {
        //UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        //PC_UUID = randomUUID().toString();
        //PC_UUID = uid.randomUUID();
        PC_UUID = "000110100001000800000805F9B34FB";
        StreamConnectionNotifier mStreamConnectionNotifier = null;
 
        try {
            mStreamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + PC_UUID);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("open err : " + e.getMessage());
        }
 
        try {
            System.out.println("Server start to listen, UUID is " + PC_UUID);
            while (true) {
                StreamConnection connection = mStreamConnectionNotifier.acceptAndOpen();
                System.out.println("new connection ");
 
                new ClientThread(connection).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("accept err : " + e.getMessage());
        }
    }
 

    private class ClientThread extends Thread {
        private StreamConnection mStreamConnection = null;
        // private DataInputStream dis = null;
        // private DataOutputStream dos = null;
        private OutputStream osm = null;
        private InputStream ism = null;
        public final static int REQUEST_GET = 0x70000001;
        public final static int REQUEST_POST = 0x70000002;
        //private int uid = 0;
        
 
        public ClientThread(StreamConnection sc) {
            mStreamConnection = sc;
            // dis = new DataInputStream(mStreamConnection.openInputStream());
            // dos = new DataOutputStream(mStreamConnection.openOutputStream());
        }

        private boolean onGet(int uid) {
            System.out.println("Handler Request Get");
            try {

                while (ism.available() > 0) {
                    //int blen = dis.available();
                    byte[] buff = new byte[ism.available()];
                    if (ism.read(buff,0,(buff.length > ism.available() ?  ism.available() : buff.length)) <= 0) {
                        System.out.println("connection close");
                        return false;
                    }
                }
                File image = new File("image/image.jpg");
                if (image.exists() == false) {
                    byte[] buf = new byte[8];
                    writeInt32(buf, 0, uid);
                    writeInt32(buf, 4, 0);
                    osm.write(buf, 0, 8);
                    osm.flush();
                    return true;
                }

                FileInputStream fis = new FileInputStream(image);
                byte[] content = new byte[(int)image.length() + 8];
                writeInt32(content, 0, uid);
                writeInt32(content, 4, (int)image.length());
                fis.read(content, 8, (int)image.length());
                osm.write(content,0,content.length);
                osm.flush();
                return true;
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("err : " + e.getMessage());
            }
            return false;
        }

        private boolean onPost(int uid) {
            System.out.println("Handler Request Post");
            try {
                byte[] bs = new byte[20];
                if (ism.read(bs,0,20) <= 0) {
                    System.out.println("connection close");
                    return false;
                }
                double longitude = readDouble(bs,0);
                double latitude = readDouble(bs,8);
                int len = readInt32(bs, 16);
                byte[] content = new byte[len];
                int clen = 0;
                int n = 0;
                do {
                    n = ism.read(content,clen,len - clen);
                    if (n <= 0) {
                        //err_ret("read stream data error!");
                        //ism.close();
                        System.out.println("connection close");
                        return false;
                    }
                    clen +=n;
                }while (clen < len);
                

                File image = new File("image/image_post.jpg");
                if (image.exists()) {
                    image.delete();
                }
                image.createNewFile();
                FileOutputStream fos = new FileOutputStream(image);
                fos.write(content, 0, len);
                fos.close();
                System.out.println("get image ok the location is (" + longitude + "," + latitude + ").");
                while (ism.available() > 0) {
                    //int blen = dis.available();
                    byte[] buff = new byte[ism.available()];
                    if (ism.read(buff,0,(buff.length > ism.available() ?  ism.available() : buff.length)) <= 0) {
                        System.out.println("connection close");
                        return false;
                    }
                }
                writeInt32(bs, 0, uid);
                writeInt32(bs, 4, 0);
                osm.write(bs, 0, 8);
                osm.flush();
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("err : " + e.getMessage());
            }
            return true;
        }

        private boolean handler_request() {
            try {
                byte[] buf = new byte[8];

                int clen = 0;
                int n = 0;
                do {
                    n = ism.read(buf,clen,8 - clen);
                    if (n <= 0) {
                        //err_ret("read stream data error!");
                        //ism.close();
                        System.out.println("connection close");
                        return false;
                    }
                    clen +=n;
                }while (clen < 8);
                // if (ism.read(buf, 0, 8) <= 0) {
                //     System.out.println("connection close");
                //     return false;
                // }
                int req = readInt32(buf, 0);
                int uid = readInt32(buf, 4);

                System.out.println("request come!");
                // dis = new DataInputStream(mStreamConnection.openInputStream());
                // dos = new DataOutputStream(mStreamConnection.openOutputStream());
                // int id = dis.readInt();
                // int req = dis.readInt();
                switch(req) {
                    case REQUEST_GET: {
                        return onGet(uid);
                    }
                    case REQUEST_POST: {
                        return onPost(uid);
                    }
                    default:{
                        System.out.println("unknown request " + req);
                        while (ism.available() > 0) {
                            //int blen = dis.available();
                            byte[] buff = new byte[ism.available()];
                            if (ism.read(buff,0,(buff.length > ism.available() ?  ism.available() : buff.length)) <= 0) {
                                System.out.println("connection close");
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("err : " + e.getMessage());
            }
            return false;
        }
 
        @Override
        public void run() {
            try {
                osm = mStreamConnection.openOutputStream();
                ism = mStreamConnection.openInputStream();
                while(handler_request());
                mStreamConnection.close();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("err : " + e.getMessage());
            }
        }
    }
}