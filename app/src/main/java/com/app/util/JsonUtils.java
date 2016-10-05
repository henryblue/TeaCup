package com.app.util;


import com.app.bean.book.Book;
import com.google.gson.Gson;


public class JsonUtils {

    public static Gson gson = new Gson();

    public static Book parseJsonData(String jsonStr) {
        Book info = gson.fromJson(jsonStr, Book.class);
        return info;
    }
}
