package com.example.mohammadali.commonexpenditure;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by mohammadali on 9/28/17.
 */


class NetworkUtils {

    static boolean hasActiveInternet() throws IOException{
        HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL("http://clients3.google.com/generate_204")).openConnection();

        return (httpURLConnection.getResponseCode() == 204 && httpURLConnection.getContentLength() == 0);
    }
}
