package com.example.mentor;

import android.bluetooth.BluetoothAdapter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
//import android.view.inputmethod.InputMethodManager;
import android.graphics.Bitmap;
import android.content.Intent;
//import java.io.*;
import java.lang.ref.WeakReference;
import android.graphics.Color;
import android.widget.Toast;
//import java.net.*;
//import java.nio.ByteBuffer;

//import com.example.mentor.ClientChannel;
//import com.example.mentor.grpcServer;
//import com.example.mentor.proto.GrpcService;
//import com.example.mentor.proto.RemoteCallGrpc;
//import com.google.protobuf.ByteString;
//import com.example.mentor.proto.GetPictureRequest;
//import com.example.mentor.proto.GetPictureResponse;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;



public class MainActivity extends AppCompatActivity {
    private Button sendButton;
    //private ImageView rcvImage;
    //private GrpcTask grpcTask;
    private GreetTask greetTask;
    private EditText hostEdit;
    private EditText portEdit;
    private EditText portServ;
    private EditText hostServ;
    private TextView show;
    private Button serverButton;
    private grpcServer server;
    private EditText btUuidEdit;
    private EditText btEdit;
    private Button btButton;
    //private Handler handler;
    private Bitmap bmForRcvImage;


    //private RunServerTask runServerTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendButton = (Button) findViewById(R.id.btn1);
        serverButton = (Button) findViewById(R.id.btn2);
        portServ = (EditText) findViewById(R.id.greet_port);
        hostServ = (EditText) findViewById(R.id.greet_host);
        //rcvImage = (ImageView) findViewById((R.id.image1));
        hostEdit = (EditText) findViewById(R.id.host);
        portEdit = (EditText) findViewById(R.id.port);
        show = (TextView) findViewById(R.id.show);
        btUuidEdit = (EditText) findViewById(R.id.bluetooth_uuid);
        btEdit = (EditText) findViewById((R.id.bluetooth_name));
        btButton = (Button) findViewById((R.id.bluetooth));
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled() == false) {
            mBluetoothAdapter.enable(); //开启
        }
//        handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                //switch (msg.what)
//                try {
//                    rcvImage.setImageBitmap((Bitmap) msg.obj);
//                }catch (Exception e) {
//                    e.printStackTrace();
//                    System.out.println("set image error : " + e.getMessage());
//                }
//            }
//        };
    }

//    public void showImage(String path) {
//        Bitmap bm = BitmapFactory.decodeFile(path);
//        if (bm == null) {
//            return;
//        }
//        handler.sendMessage(handler.obtainMessage(1,bm));
//    }

//    public String showImageByBytes(byte[] bys) {
//        if (bys == null) {
//            return "grpc failed";
//        }
//        bmForRcvImage = BitmapFactory.decodeByteArray(bys,0, bys.length);
//        if (bmForRcvImage == null) {
//            return "invalid image";
//        }
//        handler.sendMessage(handler.obtainMessage(1,bmForRcvImage));
//        return "ok";
//    }

    protected void onPause() {
        super.onPause();
//        if (runServerTask != null) {
//            runServerTask.cancel(true);
//            runServerTask = null;
//            //serverButton.setText("Start gRPC Server");
//        }
//        if (grpcTask != null) {
//            grpcTask.cancel(true);
//            grpcTask = null;
//        }
    }


//    public void resetServer() {
//        server.stop();
//        serverButton.setText("start grpc server");
//        serverButton.setEnabled(true);
//    }
//
//    public void startServer(int port) {
//        if (server == null) {
//            server = new grpcServer(port);
//        }
//        if (server.isActive()) {
//            return;
//        }
//
//        try {
//            server.start();
//        }catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("grpc server start failed : " + e.getMessage());
//            return;
//        }
//
//    }
    public void btDeviceCall(View view) {
        if (TextUtils.isEmpty(btEdit.getText().toString()) || TextUtils.isEmpty(btUuidEdit.getText().toString()) ) {
            Toast.makeText(this,"Invalid bluetooth device info!!",Toast.LENGTH_SHORT).show();
            return;
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(this, btActivity.class);
            intent.putExtra("device", btEdit.getText().toString());
            intent.putExtra("uuid", btUuidEdit.getText().toString());
            startActivity(intent);
        }else {
            show.setText("please turn on the bluetooth!");
        }

    //        sendButton.setEnabled(false);
    //        //getPicture(hostEdit.getText().toString(),portEdit.getText().toString());
    //        //resultText.setText("");
    //        grpcTask = new GrpcTask(this);
    //        grpcTask.executeOnExecutor(
    //                AsyncTask.THREAD_POOL_EXECUTOR,
    //                hostEdit.getText().toString(),
    //                portEdit.getText().toString());
    }

    public void remoteCall(View view) {
        if (TextUtils.isEmpty(portEdit.getText().toString())|| !TextUtils.isDigitsOnly(portEdit.getText().toString()) ) {
            Toast.makeText(this,"Invaild port!",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this,GrpcActivity.class);
        intent.putExtra("host",hostEdit.getText().toString());
        intent.putExtra("port",portEdit.getText().toString());
        startActivity(intent);

//        sendButton.setEnabled(false);
//        //getPicture(hostEdit.getText().toString(),portEdit.getText().toString());
//        //resultText.setText("");
//        grpcTask = new GrpcTask(this);
//        grpcTask.executeOnExecutor(
//                AsyncTask.THREAD_POOL_EXECUTOR,
//                hostEdit.getText().toString(),
//                portEdit.getText().toString());
    }

    public void ctrlServer(View view) {
        serverButton.setEnabled(false);
        serverButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
//        if (server != null && server.isActive()) {
//            resetServer();
//            return;
//        }
//        if (serverTask == null) {
//            serverTask = new GrpcServerTask(this);
//        }
        greetTask = new GreetTask(this);
        greetTask.executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                hostServ.getText().toString(),
                portServ.getText().toString());
    }


    private static class GreetTask extends AsyncTask<String, Void, String> {
        private final WeakReference<MainActivity> activityReference;

        private GreetTask(MainActivity activity) {
            this.activityReference = new WeakReference<MainActivity>(activity);
        }



        @Override
        protected String doInBackground(String... params) {
            String hostStr = params[0];
            String portStr = params[1];
            int port = TextUtils.isEmpty(portStr) ? 7777 : Integer.valueOf(portStr);

            return (new MyClient().greet(hostStr,port,"hello"));
        }

        @Override
        protected void onPostExecute(String result) {
            MainActivity activity = activityReference.get();
            if (activity == null || isCancelled()) {
                return;
            }
            TextView resultText = (TextView) activity.findViewById(R.id.show);
            Button serverButton = (Button) activity.findViewById(R.id.btn2);
            resultText.setText(result);
            serverButton.setBackgroundColor(Color.parseColor("#000000"));
            serverButton.setEnabled(true);
            //ImageView im = (ImageView) activity.findViewById(R.id.image1);
            //im.
        }

    }


//    private static class GrpcTask extends AsyncTask<String, Void, String> {
//        private final WeakReference<MainActivity> activityReference;
//
//        private GrpcTask(MainActivity activity) {
//            this.activityReference = new WeakReference<MainActivity>(activity);
//        }
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            String host = params[0];
//            //String message = params[1];
//            String portStr = params[1];
//            String result = "[" + host + ":" + portStr + "] : ";
//
//            MainActivity activity = activityReference.get();
//            if (activity == null || isCancelled()) {
//                result += "activity is over!";
//                return result;
//            }
//            int port = TextUtils.isEmpty(portStr) ? 9999 : Integer.valueOf(portStr);
//            ClientChannel c = new ClientChannel(host,port);
//            //func 2 derectly use byte[]
////            byte[] bts = c.RemoteGetPicture(1);
////            if (bts == null) {
////                result += "grpc failed!";
////                return result;
////            }
//
//            result += activity.showImageByBytes(c.RemoteGetPicture(1));
//
////            //func 1 use file
////            String path = c.GetPicture(1,activity.getApplicationContext().getFilesDir().getAbsolutePath().toString());
////
////            if (path.isEmpty()) {
////                result += "grpc failed!";
////                return result;
////            }
////
////            activity.showImage(path);
////            result += "grpc ok ";
////            result += path;
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            MainActivity activity = activityReference.get();
//            if (activity == null || isCancelled()) {
//                return;
//            }
//            TextView resultText = (TextView) activity.findViewById(R.id.show);
//            Button sendButton = (Button) activity.findViewById(R.id.btn1);
//            resultText.setText(result);
//            sendButton.setEnabled(true);
//            //ImageView im = (ImageView) activity.findViewById(R.id.image1);
//            //im.
//        }
//    }

    public static class BlueToothCtrl {
    }
}
