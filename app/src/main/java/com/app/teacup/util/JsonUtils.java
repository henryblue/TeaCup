package com.app.teacup.util;


import com.app.teacup.bean.book.Book;
import com.google.gson.Gson;


public class JsonUtils {

    private static final Gson gson = new Gson();

    public static Book parseJsonData(String jsonStr) {
        return gson.fromJson(jsonStr, Book.class);
    }
}
