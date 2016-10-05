package com.app.util;


import com.app.bean.UpdateInfo;
import com.app.bean.book.Book;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static Gson gson = new Gson();

    public static Book parseJsonData(String jsonStr) {
        Book info = gson.fromJson(jsonStr, Book.class);
        return info;
    }

    public static UpdateInfo parseUpdateJsonData(String jsonStr) throws JSONException {
        UpdateInfo info = new UpdateInfo();
        JSONObject object = new JSONObject(jsonStr);
        String version = object.getString("version");
        String name = object.getString("apkName");
        String dec = object.getString("description");
        String url = object.getString("apkUrl");

        info.setVersion(version);
        info.setApkName(name);
        info.setDec(dec);
        info.setUrl(url);
        return info;
    }
}
