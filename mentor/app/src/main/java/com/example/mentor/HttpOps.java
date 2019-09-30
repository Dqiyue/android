package com.example.mentor;

import android.net.Uri;
import android.os.StrictMode;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;


public class HttpOps {
    public static HttpRes httpPost(String strUrl, Map<String,String> params, Map<String,String> headers,byte[] content) {
        HttpRes res = new HttpRes();
        StringBuilder sbuilder = new StringBuilder();
        if (params != null && params.isEmpty() == false) {
            try {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sbuilder.append(entry.getKey()).append("=")
                            .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                            .append("&");
                }
                strUrl += '?';
                strUrl += sbuilder.toString();
            }catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                res.status = -1;
                res.result = e.getMessage();
                return res;
            }
        }
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            res.status = -1;
            res.result = e.getMessage();
            return res;
        }



        //Uri.Builder builder = new Uri.Builder();

        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream ism = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(15000);
        } catch (IOException e) {
            e.printStackTrace();
            res.status = -1;
            res.result = e.getMessage();
            return res;
        }



        try {
            //conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestMethod("POST");
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
            res.status = -1;
            res.result = e.getMessage();
            return res;
        }

        int code = 0 ;
        byte[] buf = new byte[2048];
        byte[] data = null;

        String response = "";
        try {
            //String BOUNDARY = "******";
            //String  preFix = ("/r/n--" + BOUNDARY + "--/r/n");
            //String blank = "\r\n";
            //os.write(sbuilder.toString().getBytes("UTF-8"));
//            os.write(blank.getBytes("UTF-8"));
//            os.write(blank.getBytes("UTF-8"));
//            //os.write();
//            byte[] fcontent = Base64.encode(content,0,content.length,Base64.NO_WRAP);
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
            os = conn.getOutputStream();
            if (content != null && content.length > 0) {
                os.write(content,0,content.length);
                //os.write(blank.getBytes("UTF-8"));
                os.flush();
            }

            os.close();
            res.status = conn.getResponseCode();
            if (res.status == 200) {
                ism = conn.getInputStream();
                data = readStream(ism);
            }else if (res.status >= 400){
                ism = conn.getErrorStream();
                data = readStream(ism);
                String js = new String(data);
                res.result = "verify failed : " +  js;
                return res;
            }
        } catch (IOException e) {
            e.printStackTrace();
            res.status = -1;
            res.result = "connection abort!";
            return res;
        }

        try {
            if (data != null && data.length > 0) {
                res.data = new String(data);
                //res.data = new JSONArray(js);
            }
        }catch (Exception e) {
            e.printStackTrace();
            res.status = -1;
            res.result = e.getMessage();
            return res;
        }

        conn.disconnect();

        return res;
    }


    public static String errJson(int code, String info) {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        sb.append("errno:").append(code).append(",");
        sb.append("\"error info\":").append("\"").append(info).append("\"");
        sb.append("}");

        return sb.toString();
    }


    private static byte[] readStream(InputStream ism) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = ism.read(buffer)) != -1) {
            bout.write(buffer, 0, len);
        }
        bout.close();
        ism.close();
        return bout.toByteArray();
    }
}
