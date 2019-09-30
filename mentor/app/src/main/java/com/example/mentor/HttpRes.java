package com.example.mentor;

import org.json.JSONArray;
import org.json.JSONObject;

public class HttpRes {
    public int status;
    public String result;
    public String data;
    public HttpRes() {
        status = 0;
        result = "success";
        data = null;
    }
    public JSONObject dataToJSONObject() {
        try {
            return new JSONObject(data);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public JSONArray dataToJSONArray() {
        try {
            return new JSONArray(data);
        }catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
