package com.example.mentor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.List;

public class GrpcActivity extends AppCompatActivity {
    private String host;
    private String port;
    private Button post_btn;
    private Button get_btn;
    private Handler handler;
    private ImageView img;
    private TextView result;
    private GrpcTask grpcTask;
    private File photo;
    private Bitmap bitmap;
    private ClientChannel c;
    //Location location;
    private double longitude;
    private double latitude;
    LocationListener locationListener;

    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private static final int CODE_GPS_REQUEST = 0xa2;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x04;
    private static final int GPS_PERMISSIONS_REQUEST_CODE = 0x05;
    private static final int ALL_PERMISSIONS_REQUEST_CODE = 0x06;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grpc);
        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        port = intent.getStringExtra("port");
        post_btn = (Button)findViewById(R.id.post_btn);
        get_btn = (Button)findViewById(R.id.get_btn);
        img = (ImageView)findViewById(R.id.pic_view);
        result = (TextView)findViewById(R.id.grpc_result);
        photo = null;
        bitmap = null;
        int port_num = TextUtils.isEmpty(port) ? 7777 : Integer.valueOf(port);
        c = new ClientChannel(host, port_num);
        //location = null;
        longitude = 0.0;
        latitude = 0.0;

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //switch (msg.what)
                try {
                    img.setImageBitmap((Bitmap) msg.obj);
                }catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("set image error : " + e.getMessage());
                }
            }
        };
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


//    private boolean getGpsLocation() {
//        location = null;
//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);;
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return false;
//        }
//
//        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//是否支持GPS定位
//            //获取最后的GPS定位信息，如果是第一次打开，一般会拿不到定位信息，一般可以请求监听，在有效的时间范围可以获取定位信息
//            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        }
//        return (location != null);
//    }

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            System.out.println("requestCode"+requestCode);
            switch (requestCode) {
                case CODE_CAMERA_REQUEST :{
                    String rs = null;
                    rs = "[" + host + ":" + port + " post ] : ";
                    String ss = showImage(photo.getAbsolutePath());
                    if (ss != "ok") {
                        rs += ss;
                        result.setText(rs);
                        return;
                    }

                    try {
                        FileInputStream fs = new FileInputStream(photo);
                        byte[] bs = new byte[(int)photo.length()];
                        fs.read(bs);
//                    }catch (IOException e) {
//                        e.printStackTrace();
//                        rs += ("read photo data error : " + e.getMessage());
//                        result.setText(rs);
//                        return;
//                    }
//
//                    try {
//                        double l = location.getLongitude();
//                        double a = location.getAltitude();
//                        location.

                        if (c.RemotePostPicture(1, "photo.jpg", bs, longitude, latitude) != 0) {
                            rs += "grpc failed";
                        } else {
                            rs += "grpc ok! The location is ( "  + longitude + "," + latitude + ").";
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        rs += e.getMessage();
                    }

                    result.setText(rs);
                    break;
                }
                case CODE_GPS_REQUEST: {
                    checkPermission_and_handler();
                }
            }
        }
    }

    private boolean checkPermission_gps() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    private boolean checkPermission_camera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    private void autoObtainCameraPermissionAndTakePicture() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this,"You have already refused!",Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE},CAMERA_PERMISSIONS_REQUEST_CODE);
    }

    private void autoObtainGpsPermissionAndGetLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this,"You have already refused!",Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},GPS_PERMISSIONS_REQUEST_CODE);
    }

    private void checkPermission_and_handler() {
        LocationManager m = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        if (m.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            Toast.makeText(this, "Please open the location service!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, CODE_GPS_REQUEST);
        }

        if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, ALL_PERMISSIONS_REQUEST_CODE);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ALL_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }
        realHandler();
    }

    private void realHandler() {
//        location = getLastKnownLocation();
//        if (location == null) {
//            result.setText("post failed : get gps failed!");
//            get_btn.setEnabled(true);
//            post_btn.setEnabled(true);
//            return;
//        }
        //location = loc;

        if (updateGpsLocation() == false) {
            result.setText("post failed : get gps failed!");
        }else if (takePicture() == false) {
            result.setText("post failed : take photo failed!");
        }
        get_btn.setEnabled(true);
        post_btn.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (takePicture() == false) {
                        result.setText("post failed : take photo failed!");
                    }
                } else {
                    Toast.makeText(this,"Please allow the access to Storage and Camera!",Toast.LENGTH_LONG).show();
                }
                break;
            }
            case STORAGE_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this,"Please allow the access to Storage!",Toast.LENGTH_LONG).show();
                }
                break;
            }

            case GPS_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (getGpsLocation() == false) {
//                        result.setText("post failed : get gps failed!");
//                    }

                    result.setText("post failed : get gps failed!");
                } else {
                    Toast.makeText(this,"Please allow the access to Location,or we can not provide the service!",Toast.LENGTH_LONG).show();
                }
                break;
            }

            case ALL_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 4 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    realHandler();
                } else {
                    Toast.makeText(this,"Please allow the permissions,or we can not provide the service!",Toast.LENGTH_LONG).show();
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

    public void get(View view) {
        get_btn.setEnabled(false);
        post_btn.setEnabled(false);
        grpcTask = new GrpcActivity.GrpcTask(this);
        grpcTask.executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR,
                host, port);
    }

    public void post(View view) {
        get_btn.setEnabled(false);
        post_btn.setEnabled(false);
//        if (checkPermission_gps() == false) {
//            autoObtainGpsPermissionAndGetLocation();
//            return;
//        }else {
//            if (getGpsLocation() == false) {
//                result.setText("post failed :get gps failed!");
//                get_btn.setEnabled(true);
//                post_btn.setEnabled(true);
//                return;
//            }
//            //Toast.makeText(this,"We need the permission to access location, or we can not provide the service!",Toast.LENGTH_SHORT).show();
//        }
//        if (checkPermission_camera() == false) {
//            autoObtainCameraPermissionAndTakePicture();
//        }else {
//            if (takePicture() == false) {
//                result.setText("post failed : take photo failed!");
//            }
//            get_btn.setEnabled(true);
//            post_btn.setEnabled(true);
//        }

        checkPermission_and_handler();


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

    public String showImageByBytes(byte[] bys) {
        if (bys == null) {
            return "grpc failed null data";
        }
        bitmap = BitmapFactory.decodeByteArray(bys,0, bys.length);
        if (bitmap == null) {
            return "invalid image";
        }
        handler.sendMessage(handler.obtainMessage(1,bitmap));
        return "ok";
    }

    public String showImage(String path) {
        bitmap = BitmapFactory.decodeFile(path);
        if (bitmap == null) {
            return ("invalid path " + path);
        }
        handler.sendMessage(handler.obtainMessage(1,bitmap));
        return "ok";
    }

//    public void showPhoto(File photo) {
//        bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath().toString());
//        if (bitmap == null) {
//            return;
//        }
//        handler.sendMessage(handler.obtainMessage(1,bitmap));
//    }





    private static class GrpcTask extends AsyncTask<String, Void, String> {
        private final WeakReference<GrpcActivity> activityReference;

        private GrpcTask(GrpcActivity activity) {
            this.activityReference = new WeakReference<GrpcActivity>(activity);
        }



        @Override
        protected String doInBackground(String... params) {

            String host = params[0];
            //String message = params[1];
            String portStr = params[1];
            String result = "[" + host + ":" + portStr + "] : ";

            GrpcActivity activity = activityReference.get();
            if (activity == null || isCancelled()) {
                result += "activity is over!";
                return result;
            }
            int port = Integer.valueOf(portStr);
            ClientChannel c = activity.c;

            result += activity.showImageByBytes(c.RemoteGetPicture(1));
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            GrpcActivity activity = activityReference.get();
            if (activity == null || isCancelled()) {
                return;
            }
            TextView resultText = (TextView) activity.findViewById(R.id.grpc_result);
            resultText.setText(result);
            Button postButton = (Button) activity.findViewById(R.id.post_btn);
            postButton.setEnabled(true);
            Button getButton = (Button) activity.findViewById(R.id.get_btn);
            getButton.setEnabled(true);
        }
    }
}
