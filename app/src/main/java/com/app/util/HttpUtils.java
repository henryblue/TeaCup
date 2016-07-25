package com.app.util;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    private static final int CONNECT_SUCCESS = 200;

    public static void sendHttpRequest(final String address,
                                       final HttpCallBackListener listener) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    StringBuilder response = new StringBuilder();

                    int code = connection.getResponseCode();
                    if (CONNECT_SUCCESS == code) {
                        InputStream in = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(in));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }
                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }

    public interface HttpCallBackListener {
        void onFinish(String response);

        void onError(Exception e);
    }
}
