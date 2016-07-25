package com.app.util;


import com.app.bean.BookInfo;
import com.google.gson.Gson;

public class JsonUtils {

    public static Gson gson = new Gson();

    public static BookInfo parseJsonData(String jsonStr) {
        BookInfo info = gson.fromJson(jsonStr, BookInfo.class);
        return info;
    }
}
