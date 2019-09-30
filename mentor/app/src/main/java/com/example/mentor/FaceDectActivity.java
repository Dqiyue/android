package com.example.mentor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FaceDectActivity extends AppCompatActivity {
    private Button dect;
    private Button btn_add;
    private Handler handler;
    private ImageView img;
    private TextView result;
    private EditText eName;
    private EditText eData;
    private File photo;
    private Bitmap bitmap;
    private boolean isAdd;
    private String host;
    private String port;
    private static final int MSG_SHOW_IMAGE = 1;
    private static final int MSG_POST_RESP = 2;
    private static final int MSG_ADD_PERSON = 3;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private static final int CODE_CAMERA_REQUEST = 0x04;
    private static final int CODE_CAMERA_ADD_REQUEST = 0x05;
    //private GrpcTask grpcTask;
    private ClientChannel c;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_dect);
        dect = (Button)findViewById(R.id.dect_bt);
        btn_add = (Button)findViewById(R.id.btn_add);
        eName = (EditText)findViewById(R.id.ET_NAME);
        eData = (EditText)findViewById(R.id.ET_DATA);
        img = (ImageView)findViewById(R.id.face_pic);
        result = (TextView)findViewById(R.id.dect_result);
        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        port = intent.getStringExtra("port");
        int port_num = TextUtils.isEmpty(port) ? 7777 : Integer.valueOf(port);
        c = new ClientChannel(host, port_num);
        isAdd = false;
        photo = null;
        bitmap = null;
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
                    case MSG_POST_RESP: {
                        List<String> fids = detectFaceId((byte[])msg.obj);
                        if (fids.isEmpty() == false) {
                            Map<String, Double> ret = identifyFace(fids);
                            String rt = c.RemoteFaceDectPass(ret);
                            if (rt != "ok") {
                                result.setText("identify failed! " + rt);
                            }else {
                                result.setText("identify successfully!");
                            }

                        }
                        dect.setEnabled(true);
                        btn_add.setEnabled(true);
                        break;
                    }
                    case MSG_ADD_PERSON :{
                        String personId = addPerson();
                        if (personId != null) {
                            String persistedFaceId = addFaceToPerson((byte[])msg.obj,personId);
                            if (persistedFaceId != null && trainPersonGroup()) {
                                result.setText("Add " + eName.getText().toString() + " success!");
                            }
                        }
                        dect.setEnabled(true);
                        btn_add.setEnabled(true);
                        break;
                    }
                    default:{
                        System.out.println("unknown message!");
                        break;
                    }
                }
            }
        };
        btn_add.setEnabled(true);
        dect.setEnabled(true);
    }

    public void onAddPerson(View view) {
        btn_add.setEnabled(false);
        dect.setEnabled(false);
        isAdd = true;
        if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST_CODE);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ALL_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }

        if (takePicture() == false) {
            btn_add.setEnabled(true);
            dect.setEnabled(true);
            result.setText("take face photo failed!");
            return;
        }
    }


    public void onFaceDect(View view) {
        dect.setEnabled(false);
        btn_add.setEnabled(false);
        isAdd = false;
        if (c.isReady() == false) {
            result.setText("system can not access mentor device!");
            dect.setEnabled(true);
            btn_add.setEnabled(true);
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST_CODE);
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},ALL_PERMISSIONS_REQUEST_CODE);
                return;
            }
        }

        if (takePicture() == false) {
            btn_add.setEnabled(true);
            dect.setEnabled(true);
            result.setText("take face photo failed!");
            return;
        }
    }

    private String addPerson() {
        String name = eName.getText().toString();
        String userData = eData.getText().toString();
        try {
            //handler.sendMessage(handler.obtainMessage(MSG_POST_RESP,bs));
            Map<String,String> headers = new TreeMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Ocp-Apim-Subscription-Key", "e942658885d142eaa93025343295725d");
            String durl = "https://unlockmentorutbyface.cognitiveservices.azure.com/face/v1.0/persongroups/mentor/persons";
            JSONObject js = new JSONObject();
            js.put("name", name);
            js.put("userData", userData);
            HttpRes res = HttpOps.httpPost(durl,null,headers,js.toString().getBytes("utf-8"));
            if (res.status == 200) {
                return ((String)res.dataToJSONObject().get("personId"));
            }else {
                result.setText("add person failed : " + res.result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //rs += e.getMessage();
            result.setText("add person error : " + e.getMessage());
        }
        return null;
    }

    private String addFaceToPerson(byte[] face, String personId) {
        try {
            //handler.sendMessage(handler.obtainMessage(MSG_POST_RESP,bs));
            Map<String,String> headers = new TreeMap<String, String>();
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Ocp-Apim-Subscription-Key", "e942658885d142eaa93025343295725d");
            String durl = "https://unlockmentorutbyface.cognitiveservices.azure.com/face/v1.0/persongroups/mentor/persons/" + personId + "/persistedFaces";
            HttpRes res = HttpOps.httpPost(durl,null,headers,face);
            if (res.status == 200) {
                return ((String)res.dataToJSONObject().get("persistedFaceId"));
            }else {
                result.setText("add face to person failed : " + res.result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //rs += e.getMessage();
            result.setText("add face to person error : " + e.getMessage());
        }
        return null;
    }

    private boolean trainPersonGroup() {
        try {
            //handler.sendMessage(handler.obtainMessage(MSG_POST_RESP,bs));
            Map<String,String> headers = new TreeMap<String, String>();
            headers.put("Ocp-Apim-Subscription-Key", "e942658885d142eaa93025343295725d");
            String durl = "https://unlockmentorutbyface.cognitiveservices.azure.com/face/v1.0/persongroups/mentor/train";
            HttpRes res = HttpOps.httpPost(durl,null,headers,null);
            if (res.status == 202) {
                return true;
            }else {
                result.setText("train person group failed : " + res.result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //rs += e.getMessage();
            result.setText("train person group error : " + e.getMessage());
        }
        return false;
    }

    private List<String> detectFaceId(byte[] face) {
        List<String> faceIds = new LinkedList<String>();
        try {
            //handler.sendMessage(handler.obtainMessage(MSG_POST_RESP,bs));
            Map<String,String> headers = new TreeMap<String, String>();
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Ocp-Apim-Subscription-Key", "e942658885d142eaa93025343295725d");
            Map<String,String> params = new TreeMap<String, String>();
            params.put("returnFaceId", "true");
            //params.put("returnFaceLandmarks", "false");
            //params.put("returnFaceAttributes", "age,gender");
            params.put("recognitionModel", "recognition_02");
            params.put("returnRecognitionModel", "false");
            params.put("detectionModel", "detection_02");
            String durl = "https://unlockmentorutbyface.cognitiveservices.azure.com/face/v1.0/detect";
            HttpRes res = HttpOps.httpPost(durl,params,headers,face);
            if (res.status == 200) {
                JSONArray jarr = res.dataToJSONArray();
                if (jarr == null) {
                    result.setText("detect failed : null return data");
                    return faceIds;
                }
                for (int i = 0; i < jarr.length(); ++i) {
                    String id = (String) jarr.getJSONObject(i).get("faceId");
                    faceIds.add(id);
                }
            }else {
                result.setText("detect failed : " + res.result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //rs += e.getMessage();
            result.setText("face dect error : " + e.getMessage());
        }
        return faceIds;
    }

    private Map<String, Double> identifyFace(List<String> faceIds)  {
        //List<String> faceIds = new LinkedList<String>();
        Map<String, Double> ret = new TreeMap<String, Double>();
        if (faceIds.isEmpty()) {
            return ret;
        }
        try {
            //handler.sendMessage(handler.obtainMessage(MSG_POST_RESP,bs));
            Map<String,String> headers = new TreeMap<String, String>();
            headers.put("Content-Type", "application/json");
            headers.put("Ocp-Apim-Subscription-Key", "e942658885d142eaa93025343295725d");
            //Map<String,String> params = new TreeMap<String, String>();
            String durl = "https://unlockmentorutbyface.cognitiveservices.azure.com/face/v1.0/identify";
            JSONObject js = new JSONObject();
            js.put("personGroupId", "mentor");
            js.put("confidenceThreshold", 0.6);
            JSONArray ids = new JSONArray();
            //String[] ids = new String[faceIds.size()];
            for (int i = 0; i < faceIds.size(); ++i) {
                ids.put(faceIds.get(i));
            }
            js.put("faceIds",ids);
            //js.put("faceIds",new String[] {"c509c589-2ae0-4de8-8853-3a23d9d6c384"});
            String jsonStr = js.toString();
            HttpRes res = HttpOps.httpPost(durl,null,headers,jsonStr.getBytes("UTF-8"));
            if (res.status == 200) {
                JSONArray jarr = res.dataToJSONArray();
                if (jarr == null) {
                    result.setText("identify  failed : null return data");
                    return ret;
                }
                for (int i = 0; i < jarr.length(); ++i) {
                    JSONArray item = jarr.getJSONObject(i).getJSONArray("candidates");
                    for (int j = 0; j < item.length(); ++j) {
                        JSONObject obj = item.getJSONObject(j);
                        String id = (String) obj.get("personId");
                        Double v = (Double)obj.get("confidence");
                        ret.put(id,v);
                    }
                }
            }else {
                result.setText("identify  failed : " + res.result);
            }
        }catch (Exception e) {
            e.printStackTrace();
            //rs += e.getMessage();
            result.setText("face identify error : " + e.getMessage());
        }
        return ret;
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


        intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
        intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        //intent.putExtra("return-data", true);
        startActivityForResult(intent, CODE_CAMERA_REQUEST);
        btn_add.setEnabled(true);
        dect.setEnabled(true);
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            System.out.println("requestCode"+requestCode);
            switch (requestCode) {
                case CODE_CAMERA_REQUEST :{
                    btn_add.setEnabled(false);
                    dect.setEnabled(false);
                    String ss = showImage(photo.getAbsolutePath());
                    if (ss != "ok") {
                        result.setText(ss);
                        btn_add.setEnabled(true);
                        dect.setEnabled(true);
                        return;
                    }

                    try {
                        FileInputStream fs = new FileInputStream(photo);
                        byte[] bs = new byte[(int)photo.length()];
                        fs.read(bs);
                        if (isAdd == false) {
                            result.setText("identifying.....");
                            handler.sendMessage(handler.obtainMessage(MSG_POST_RESP, bs));
                        }else {
                            result.setText("processing.....");
                            handler.sendMessage(handler.obtainMessage(MSG_ADD_PERSON, bs));
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                        //rs += e.getMessage();
                        result.setText("get photo error : " + e.getMessage());
                        btn_add.setEnabled(true);
                        dect.setEnabled(true);
                    }
                    break;
                }
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
                    if (takePicture() == false) {
                        result.setText("take photo failed!");
                    }
                } else {
                    Toast.makeText(this,"Please allow the access to Storage and Camera!",Toast.LENGTH_LONG).show();
                }
                btn_add.setEnabled(true);
                dect.setEnabled(true);
                break;
            }
        }
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


//    private static class GrpcTask extends AsyncTask<String, Void, String> {
//        private final WeakReference<FaceDectActivity> activityReference;
//
//        private GrpcTask(FaceDectActivity activity) {
//            this.activityReference = new WeakReference<FaceDectActivity>(activity);
//        }
//
//
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            String faceId = params[0];
//            //String message = params[1];
//
//
//            FaceDectActivity activity = activityReference.get();
//            if (activity == null || isCancelled()) {
//                return "activity is over!";
//            }
//            ClientChannel c = activity.c;
//
//            return c.RemoteFaceDectPass(faceId);
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            FaceDectActivity activity = activityReference.get();
//            if (activity == null || isCancelled()) {
//                return;
//            }
//            TextView resultText = (TextView) activity.findViewById(R.id.dect_result);
//            resultText.setText(result);
//            Button bt = (Button) activity.findViewById(R.id.dect_bt);
//            bt.setEnabled(true);
//        }
//    }


}
