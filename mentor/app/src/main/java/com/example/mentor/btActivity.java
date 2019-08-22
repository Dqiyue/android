package com.example.mentor;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.UUID;


import android.content.Intent;
import android.widget.Toast;

public class btActivity extends AppCompatActivity {
    private String bt_dev;
    private  String bt_uuid;
    private String bt_tag;
    private Button post_btn;
    private Button get_btn;
    private Handler handler;
    private ImageView img;
    private TextView result;
    private File photo;
    private Bitmap bitmap;
    private int uid;
    private double longitude;
    private double latitude;
    private boolean findmatch;


    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice dev;
    private BluetoothSocket bt_socket;
    private BroadcastReceiver mBroadcastReceiver;
    private PostTask ptask;

    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private static final int CODE_GPS_REQUEST = 0xa2;
    private static final int CODE_BLUETOOTH_REQUEST = 0xa3;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x04;
    private static final int GPS_PERMISSIONS_REQUEST_CODE = 0x05;
    private static final int BLUETOOTH_PERMISSIONS_REQUEST_CODE = 0x06;
    private static final int ALL_PERMISSIONS_REQUEST_CODE = 0x07;

    private static final byte CODE_START_ONE = 0x0a;
    private static final byte CODE_START_TWO = 0x0b;
    private static final byte CODE_END_ONE = 0x7E;
    private static final byte CODE_END_TWO = 0x7F;
    private static final byte CODE_REQ_GET = 'G';
    private static int REQUEST_GET = 0x70000001;
    private static int REQUEST_POST = 0x70000002;

    private static final int MSG_SHOW_IMAGE = 1;
    private static final int MSG_POST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);
        Intent intent = getIntent();
        bt_dev = intent.getStringExtra("device");
        bt_uuid = intent.getStringExtra("uuid");
        post_btn = (Button) findViewById(R.id.post_btn_bt);
        get_btn = (Button) findViewById(R.id.get_btn_bt);
        result = (TextView) findViewById(R.id.stdout);
        img = (ImageView) findViewById(R.id.pic_view_bt);
        uid = 0;

        get_btn.setEnabled(false);
        post_btn.setEnabled(false);

        bt_socket = null;
        bt_tag = "YOUR DEVICE";
        InitBlueTooth();
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_SHOW_IMAGE: {
                        try {
                            img.setImageBitmap((Bitmap) msg.obj);
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("set image error : " + e.getMessage());
                        }
                        break;
                    }
                    case MSG_POST: {
                        sendPostReq();
                        break;
                    }
                    default:{
                        System.out.println("unknown message!");
                        break;
                    }
                }
            }
        };
    }




    private static class PostTask extends AsyncTask<String, Void, String> {
        private final WeakReference<btActivity> activityReference;

        private PostTask(btActivity activity) {
            this.activityReference = new WeakReference<btActivity>(activity);
        }


        @Override
        protected String doInBackground(String... params) {
            btActivity activity = activityReference.get();
            if (activity == null || isCancelled()) {
                return "";
            }

            return activity.sendPostReq();
        }

        @Override
        protected void onPostExecute(String result) {
            btActivity activity = activityReference.get();
            if (activity == null || isCancelled()) {
                return;
            }
            TextView resultText = (TextView) activity.findViewById(R.id.stdout);
            Button post = (Button) activity.findViewById(R.id.post_btn_bt);
            Button get = (Button) activity.findViewById(R.id.get_btn_bt);
            if (result == "ok") {
                post.setEnabled(true);
                get.setEnabled(true);
                resultText.setText("post success !");
            }else if (result == "failed") {
                post.setEnabled(true);
                get.setEnabled(true);
                resultText.setText("post failed !");
            }else {
                post.setEnabled(false);
                get.setEnabled(false);
                resultText.setText("connection abort");
            }
        }
    }


    @Override
    protected void onDestroy() {
        try{
            if (mBroadcastReceiver != null) {
                unregisterReceiver(mBroadcastReceiver);
            }
            if (bt_socket != null && bt_socket.isConnected()) {
                bt_socket.close();
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("close socket err : " + e.getMessage());
        }
        super.onDestroy();
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

    public String sendPostReq() {
        try {
            InputStream ism = bt_socket.getInputStream();
            while (ism.available() > 0) {
                //int blen = dis.available();
                byte[] buff = new byte[ism.available()];
                if (ism.read(buff,0,buff.length) <= 0) {
                    //connection_abort();
                    return "err";
                }
            }
            FileInputStream fs = new FileInputStream(photo);
            byte[] bs = new byte[(int)photo.length() + 28];
            writeInt32(bs,0,REQUEST_POST);
            writeInt32(bs,4,uid);
            writeDouble(bs,8,longitude);
            writeDouble(bs,16,latitude);
            writeInt32(bs,24,(int)photo.length());
            fs.read(bs,28,(int)photo.length());
            OutputStream osm = bt_socket.getOutputStream();
            osm.write(bs,0,bs.length);
            osm.flush();
            //InputStream ism = bt_socket.getInputStream();
            if (ism.read(bs,0,8) <= 0) {
                //connection_abort();
                return "err";
            }
            int id = readInt32(bs,0);
            if (id != uid) {
                //err_ret("Error response!");
                return "failed";
            }

            int status = readInt32(bs,4);
            if (status != 0) {
                //err_ret("Post failed : " + status);
                return "failed";
            }

        }catch (Exception e) {
            e.printStackTrace();
            //err_ret("err : " + e.getMessage());
            return "err";
        }

        return "ok";
        //err_ret("post ok! location (" + longitude + "," + latitude + ")." );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            System.out.println("requestCode"+requestCode);
            switch (requestCode) {
                case CODE_CAMERA_REQUEST :{
                    String rs = null;
                    String ss = showImage(photo.getAbsolutePath());
                    if (ss != "ok") {
                        rs += ss;
                        result.setText(rs);
                        return;
                    }
                    result.setText("System is posting the image and location(" + longitude + "," + latitude + "), please wait for a moment ......");
                    ptask = new PostTask(this);
                    ptask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //handler.sendMessage(handler.obtainMessage(MSG_POST,null));
                    break;
                }
                case CODE_GPS_REQUEST: {
                    //checkPermission_and_handler();
                    if (checkAndRequestAllPermissions() == false) {
                        break;
                    }

                    takePictureAndUpdateGps();
                    break;
                }
                case CODE_BLUETOOTH_REQUEST: {
                    connectBlueToothDevice();
//                    if (true == checkAndRequestBlueToothPermission()) {
//                        connectBlueToothDevice();
//                    }
                    break;
                }

            }
        }else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case CODE_CAMERA_REQUEST :{
                    break;
                }
                case CODE_GPS_REQUEST:{
                    LocationManager m = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
                    if (m.isProviderEnabled(LocationManager.GPS_PROVIDER) == true) {
                        if (checkAndRequestAllPermissions() == false) {
                            break;
                        }
                        if (bt_socket == null || bt_socket.isConnected() == false) {
                            connectBlueToothDevice();
                            break;
                        }
                        takePictureAndUpdateGps();
                    }else {
                        err_ret("please turn on the location service!");
                    }
                    break;
                }
                case CODE_BLUETOOTH_REQUEST: {
                    if (mBluetoothAdapter.isEnabled()) {
                        if (checkAndTurnOnGps() == true) {
                            connectBlueToothDevice();
                        }
                    }else {
                        result.setText("please turn on the bluetooth!");
                        finish();
                    }
//                    if (true == checkAndRequestBlueToothPermission()) {
//                        connectBlueToothDevice();
//                    }
                    break;
                }
            }
        }
    }

    private boolean checkAndTurnOnBlueTooth() {
        if (Build.VERSION.SDK_INT >= 18) {
//            BluetoothManager bm = (BluetoothManager) this.getSystemService(this.BLUETOOTH_SERVICE);
//            mBluetoothAdapter = bm.getAdapter();
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                //Toast.makeText(this,"Your system does not support bluetooth!",Toast.LENGTH_SHORT).show();
                result.setText("Your system does not support bluetooth!");
                return false;
            }
            if (mBluetoothAdapter.isEnabled() == false) {
//                Toast.makeText(this, "Please open the bluetooth service!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent();
//                intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
//                startActivityForResult(intent, CODE_BLUETOOTH_REQUEST);
//                return false;
                result.setText("please turn on the bluetooth!");
                return false;
                //mBluetoothAdapter.enable();
            }
            return true;
        }
        result.setText("Your system does not support bluetooth!");
        //Toast.makeText(this,"Your system does not support bluetooth!",Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkAndTurnOnGps() {
        LocationManager m = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        if (m.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            Toast.makeText(this, "Please open the location service!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, CODE_GPS_REQUEST);
            return false;
        }
        return true;
    }

    private boolean checkAndRequestBlueToothPermission() {
        if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_PERMISSIONS_REQUEST_CODE);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ALL_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }


    private boolean checkAndRequestAllPermissions() {
        if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, ALL_PERMISSIONS_REQUEST_CODE);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ALL_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private boolean checkAndRequestGpsPermissions() {
        if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, GPS_PERMISSIONS_REQUEST_CODE);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ALL_PERMISSIONS_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }


    private void InitBlueTooth() {
        if (false == checkAndTurnOnBlueTooth()) {
            return;
        }
        if (false == checkAndRequestGpsPermissions()) {
            return;
        }
        if (false == checkAndTurnOnGps()) {
            return;
        }
        connectBlueToothDevice();
    }

    private BluetoothDevice getPairedDevices() {
        // 获得和当前Android已经配对的蓝牙设备。
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            // 遍历
            for (BluetoothDevice device : pairedDevices) {
                // 把已经取得配对的蓝牙设备名字和地址打印出来。
                Log.d(bt_tag, device.getName() + " : " + device.getAddress());
                if (TextUtils.equals(bt_dev, device.getName())) {
                    Log.d(bt_tag, "已配对目标设备 -> " + bt_dev);
                    return device;
                }
            }
        }

        return null;
    }


    private void connectBlueToothDevice() {
        dev = getPairedDevices();
        if (dev == null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.setPriority(Integer.MAX_VALUE);
            //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        if (bt_socket != null && bt_socket.isConnected()) {
                            return;
                        }
                        dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        String name = dev.getName();
                        String addr = dev.getAddress();
                        if (name != null || addr != null) {
                            Log.d(bt_tag, "发现设备:" + name + " " + addr);
                        }

                        if (name != null && name.equals(bt_dev)) {
                            Log.d(bt_tag, "find target bluetooth device, try to connect");
                            findmatch = true;
                            // 蓝牙搜索是非常消耗系统资源开销的过程，一旦发现了目标感兴趣的设备，可以关闭扫描。
//                            if (mBluetoothAdapter.isDiscovering()) {
//                                mBluetoothAdapter.cancelDiscovery();
//                            }
                            try {
                                bt_socket = dev.createInsecureRfcommSocketToServiceRecord(UUID.fromString(bt_uuid));
                            }catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("create bluetooth socket err : " + e.getMessage());
                                result.setText("Connect to " + bt_dev + " failed!");
                                return;
                            }

                            try {
                                bt_socket.connect();
                                if (bt_socket.isConnected()) {
                                    result.setText("Connected to " + bt_dev + ", system is ready.");
                                    get_btn.setEnabled(true);
                                    post_btn.setEnabled(true);
                                } else {
                                    result.setText("Connected to " + bt_dev + ", system failed!.");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("connect to target bluetooth device error : " + e.getMessage());

                                try {
                                    bt_socket =(BluetoothSocket) dev.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(dev,1);
                                    bt_socket.connect();
                                    if (bt_socket.isConnected()) {
                                        result.setText("Connected to " + bt_dev + ", system is ready.");
                                        get_btn.setEnabled(true);
                                        post_btn.setEnabled(true);
                                    } else {
                                        result.setText("Connected to " + bt_dev + ", system failed!.");
                                    }
                                }catch (Exception ef) {
                                    ef.printStackTrace();
                                    System.out.println("fallback error : " + ef.getMessage());
                                    result.setText("Connect to " + bt_dev + " failed!");
                                }
                            }

                            if (mBluetoothAdapter.isDiscovering()) {
                                mBluetoothAdapter.cancelDiscovery();
                            }
                        }
                    }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                        if (findmatch == false) {
                            result.setText("find no match device!");
                        }
                    }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        result.setText("start to find match device ....");
                    }
                }
            };

            registerReceiver(mBroadcastReceiver, filter);
            findmatch = false;
            if (mBluetoothAdapter.startDiscovery()) {
                Log.d(bt_tag, "start searching bluetooth device...");
            }
        }else {
            try {
                bt_socket = dev.createInsecureRfcommSocketToServiceRecord(UUID.fromString(bt_uuid));
                bt_socket.connect();
                if (bt_socket.isConnected()) {
                    result.setText("Connected to " + bt_dev + ", system is ready.");
                    get_btn.setEnabled(true);
                    post_btn.setEnabled(true);
                } else {
                    result.setText("Connected to " + bt_dev + ", system failed!.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("connect to target bluetooth device error : " + e.getMessage());
                result.setText("Connect to " + bt_dev + " failed!");
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case BLUETOOTH_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    connectBlueToothDevice();
                } else {
                    err_ret("Please allow the BlueTooth access,or we can not provide the service!");
                }
                break;
            }
            case GPS_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if (checkAndTurnOnGps() == true) {
                        connectBlueToothDevice();
                    }
                }else {
                    result.setText("Please allow the Gps access,or we can not provide the service!");
                }
                break;
            }
            case ALL_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    //realHandler();
                    //connectBlueToothDevice();
                    if (bt_socket == null || bt_socket.isConnected() == false) {
                        connectBlueToothDevice();
                        break;
                    }
                    takePictureAndUpdateGps();
                } else {
                    err_ret("Please allow the permissions,or we can not provide the service!");
                    //Toast.makeText(this,"Please allow the permissions,or we can not provide the service!",Toast.LENGTH_LONG).show();
                }
                break;
            }

        }

//        get_btn.setEnabled(true);
//        post_btn.setEnabled(true);

//        if (checkPermission() == false) {
//            Toast.makeText(this,"Please allow the access to Storage and camera or we can not work!",Toast.LENGTH_LONG).show();
//        }
    }

    private void err_ret(String msg) {
        result.setText(msg);
        get_btn.setEnabled(true);
        post_btn.setEnabled(true);
    }

    private void connection_abort() {
        result.setText("connection abort!");
        get_btn.setEnabled(false);
        post_btn.setEnabled(false);
    }

    private void clear_stream(DataInputStream dis) {
        try {
            while (dis.available() > 0) {

            }
        }catch(Exception e) {

        }
    }

    public String showImageByBytes(byte[] bys) {
        if (bys == null) {
            return "null data";
        }
        bitmap = BitmapFactory.decodeByteArray(bys,0, bys.length);
        if (bitmap == null) {
            return "invalid image";
        }
        handler.sendMessage(handler.obtainMessage(MSG_SHOW_IMAGE,bitmap));
        return "Get image successfully!";
    }

    public String showImage(String path) {
        bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            return ("invalid path " + path);
        }
        handler.sendMessage(handler.obtainMessage(MSG_SHOW_IMAGE,bitmap));
        return "ok";
    }

    //确保dst长度足够此处不做判断
    public void writeInt32(byte[] dst, int off, int v) {
        dst[off] = (byte)(v >> 24 & 0xff);
        dst[off + 1] = (byte)(v >> 16 & 0xff);
        dst[off + 2] = (byte)(v >> 8 & 0xff);
        dst[off + 3] = (byte)(v & 0xff);
    }

    public int readInt32(byte[] src, int off) {
        int ret = ((int)src[off]) << 24;
        ret += ((int)src[off + 1]) << 16;
        ret += ((int)src[off + 2]) << 8;
        ret += (int) src[off + 3];
        return ret;
    }

    public void onGet(View view) {
        get_btn.setEnabled(false);
        post_btn.setEnabled(false);
        if (true == bt_socket.isConnected()) {
            try {
                OutputStream osm = bt_socket.getOutputStream();
                InputStream ism = bt_socket.getInputStream();
                // clear read io
                while (ism.available() > 0) {
                        //int blen = dis.available();
                    byte[] buff = new byte[ism.available()];
                    if (ism.read(buff,0,buff.length) <= 0) {
                        connection_abort();
                        return;
                    }
                }
                byte[] buf = new byte[8];
                writeInt32(buf,0,REQUEST_GET);
                writeInt32(buf,4,uid);
                osm.write(buf,0,8);
                osm.flush();

                if (ism.read(buf,0,8) <= 0) {
                    connection_abort();
                    return;
                }
                int id = readInt32(buf,0);
                if (id != uid) {
                    err_ret("Error response!");
                    return;
                }
                int len = readInt32(buf,4);
                if (len <= 0) {
                    err_ret("no data!");
                    return;
                }
                byte[] content = new byte[len];
                int clen = 0;
                int n = 0;
                do {
                    n = ism.read(content,clen,len - clen);
                    if (n <= 0) {
                        //err_ret("read stream data error!");
                        //ism.close();
                        connection_abort();
                        return;
                    }
                    clen +=n;
                }while (clen < len);
                uid = (++uid) & 0x7fffffff;
                result.setText(showImageByBytes(content));
                //osm.flush();
//                DataOutputStream dos = new DataOutputStream(bt_socket.getOutputStream());
//                DataInputStream dis = new DataInputStream(bt_socket.getInputStream());
//                dos.write(uid);
//                dos.write(REQUEST_GET);
//                dos.close();
//                if (dis.readInt() != uid) {
//                    while (dis.available() > 0) {
//                        //int blen = dis.available();
//                        byte[] buf = new byte[dis.available()];
//                        dis.read(buf,0,buf.length);
//                    }
//                    err_ret("Error response!");
//                    dis.close();
//                    return;
//                }
//
//                int len = dis.readInt();
//                if (len <= 0) {
//                    while (dis.available() > 0) {
//                        //int blen = dis.available();
//                        byte[] buf = new byte[dis.available()];
//                        dis.read(buf,0,buf.length);
//                    }
//                    err_ret("no data!");
//                    dis.close();
//                    return;
//                }
//                byte[] content = new byte[len];
//                int clen = 0;
//                int n = 0;
//                do {
//                    n = dis.read(content,clen,len - clen);
//                    if (n <= 0) {
//                        err_ret("read stream data error!");
//                        dis.close();
//                        return;
//                    }
//                    clen +=n;
//                }while (clen < len);
//                dis.close();
//                uid = (++uid) & 0x7fffffff;
//                result.setText(showImageByBytes(content));
            }catch (Exception e) {
                e.printStackTrace();
                System.out.println("Get error : " + e.getMessage());
            }
        }else {
            result.setText("connection is abort!!");
        }
        get_btn.setEnabled(true);
        post_btn.setEnabled(true);
//        grpcTask = new GrpcActivity.GrpcTask(this);
//        grpcTask.executeOnExecutor(
//                AsyncTask.THREAD_POOL_EXECUTOR,
//                host, port);
    }


    private boolean updateGpsLocation() {
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setAccuracy(Criteria.ACCURACY_LOW);//设置低精度
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
        Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
        if (location == null && (location = getLastKnownLocation()) == null) {
            Toast.makeText(this,"Bad gps signal!",Toast.LENGTH_SHORT).show();
        } else {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        //updateToNewLocation(location);

        // 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
//        locationManager.requestLocationUpdates(provider, 100 * 1000, 500,
//                locationListener);

        return true;
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }



    private boolean takePicture() {
//        String path = getFilesDir().getAbsolutePath().toString();
//        path += (File.separatorChar + "photo.jpg");
        photo = new File(getExternalCacheDir(), "photo.jpg");
        try {
            if (photo.exists()) {
                photo.delete();
            }
            photo.createNewFile();
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("new photo file " + " error : " + e.getMessage());
            return false;
        }

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false) {
            Toast.makeText(this,"You have no sd card!",Toast.LENGTH_SHORT).show();
            return false;
        }
        Uri photoUri = null;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        photoUri = Uri.fromFile(photo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            photoUri = FileProvider.getUriForFile(this, "com.example.mentor.fileprovider", photo);
        }
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                photoUri = FileProvider.getUriForFile(GrpcActivity.this, "com.example.mentor.fileprovider", photo);
//            } else {
//                photoUri = Uri.fromFile(photo);
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("getUriForFile error : " + e.getMessage());
//            return false;
//        }


        intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        //intent.putExtra("return-data", true);
        startActivityForResult(intent, CODE_CAMERA_REQUEST);
        return true;
    }


    private void takePictureAndUpdateGps() {
        if (updateGpsLocation() == false) {
            err_ret("post failed : get gps failed!");
            return;
        }else if (takePicture() == false) {
            err_ret("post failed : take photo failed!");
            return;
        }
    }

    public void onPost(View view) {
        post_btn.setEnabled(false);
        get_btn.setEnabled(false);

        if (true == bt_socket.isConnected()) {

            if (checkAndTurnOnGps() == false || checkAndRequestAllPermissions() == false) {
                return;
            }

            if(updateGpsLocation() == false) {
                result.setText("post failed : get gps failed!");
            }else if (takePicture() == false) {
                result.setText("post failed : take photo failed!");
            }
        }else {
            result.setText("connection is abort!!");
        }
        post_btn.setEnabled(true);
        get_btn.setEnabled(true);
//        if (checkPermission() == false) {
//            autoObtainCameraPermissionAndTakePicture();
//        }else {
//            if (takePicture() == false) {
//                result.setText("post failed : take photo failed!");
//            }
//            get_btn.setEnabled(true);
//            post_btn.setEnabled(true);
//        }


//        if (takePicture() == false) {
//            get_btn.setEnabled(true);
//            post_btn.setEnabled(true);
//            result.setText("post failed : take photo failed!");
//            return;
//        }


//        grpcTask = new GrpcActivity.GrpcTask(this);
//        grpcTask.executeOnExecutor(
//                AsyncTask.THREAD_POOL_EXECUTOR,
//                host, port, "post");
    }

}
