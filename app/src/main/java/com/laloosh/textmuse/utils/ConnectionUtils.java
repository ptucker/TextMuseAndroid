package com.laloosh.textmuse.utils;

import android.util.Log;

import com.laloosh.textmuse.app.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.io.IOUtils;

public class ConnectionUtils {

    public static final int READ_TIMEOUT = 120000;  //120 seconds for read timeout
    public static final int CONNECT_TIMEOUT = 30000;  //30 seconds for connect timeout

    public byte[] getUrlBytes(String stringUrl, HashMap<String, String> params) {
        InputStream is = null;
        byte[] result = null;

        try {

            if (params != null && params.size() > 0) {
                String paramString = getParamsString(params);
                stringUrl += "?" + paramString;
            }

            HttpURLConnection conn = openConnection(stringUrl);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.connect();

            int response = conn.getResponseCode();
            if (response != 200) {
                Log.e(Constants.TAG, "Got an unexpected response from the server, URL was: " + stringUrl + " and response was: " + Integer.toString(response));
                return null;
            }

            is = conn.getInputStream();
            result = IOUtils.toByteArray(is);

        } catch (Exception e) {
            Log.e(Constants.TAG, "Error getting URL using parameters", e);
            return null;
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }

        return result;
    }

    public String getUrl(String stringUrl, HashMap<String, String> params) {

        byte[] result = getUrlBytes(stringUrl, params);

        return byteArrayToString(result);
    }

    private String byteArrayToString(byte[] input) {
        if (input != null) {
            String resultString;

            try {
                resultString = new String(input, "UTF-8");
                return resultString;
            } catch (UnsupportedEncodingException e) {
                //This shouldn't happen
                Log.e(Constants.TAG, "Could not convert web data to UTF8");
            }
        }

        return null;
    }

    public String postUrl(String stringUrl, HashMap<String, String> params) {

        byte[] result = null;
        result = postUrlBytes(stringUrl, params);

        return byteArrayToString(result);
    }

    public byte[] postUrlBytes(String stringUrl, HashMap<String, String> params) {
        InputStream is = null;
        byte[] result = null;

        try {

            HttpURLConnection conn = openConnection(stringUrl);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setRequestMethod("POST");

            conn.setDoInput(true);
            conn.setDoOutput(true);

            if (params != null && !params.isEmpty()) {
                StringBuilder postData = new StringBuilder();
                for (HashMap.Entry<String, String> entry : params.entrySet()) {
                    try {
                        if (postData.length() > 0) {
                            postData.append('&');
                        }
                        String key = URLEncoder.encode(entry.getKey(), "UTF-8");
                        String value = URLEncoder.encode(entry.getValue(), "UTF-8");

                        postData.append(key);
                        postData.append("=");
                        postData.append(value);

                    } catch (Exception e) {
                        Log.e(Constants.TAG, "Post: could not attach key: " + entry.getKey() + " and value: " + entry.getValue());
                    }
                }

                if (postData.length() > 0) {
                    byte[] postDataBytes = postData.toString().getBytes("UTF-8");
                    conn.getOutputStream().write(postDataBytes);
                }
            }

            int response = conn.getResponseCode();
            if (response != 200) {
                Log.e(Constants.TAG, "Got an unexpected response from the server during post, URL was: " + stringUrl + " and response was: " + Integer.toString(response));
                return null;
            }

            is = conn.getInputStream();
            result = IOUtils.toByteArray(is);

        } catch (Exception e) {
            Log.e(Constants.TAG, "Error posting URL using parameters", e);
            return null;
        } finally {
            if (is != null) {
                IOUtils.closeQuietly(is);
            }
        }

        return result;
    }

//    public byte[] getUrlBytes(String stringUrl, HashMap<String, String> params) {
//        InputStream is = null;
//        byte[] result = null;
//
//        try {
//            HttpURLConnection conn = openConnection(stringUrl);
//            conn.setReadTimeout(READ_TIMEOUT);
//            conn.setConnectTimeout(CONNECT_TIMEOUT);
//            conn.setRequestMethod("GET");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//
//            if (params != null && !params.isEmpty()) {
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                writer.write(getParamsString(params));
//                writer.close();
//                os.close();
//            }
//
//            conn.connect();
//
//            int response = conn.getResponseCode();
//            if (response != 200) {
//                Log.e(Constants.TAG, "Got an unexpected response from the server, URL was: " + stringUrl + " and response was: " + Integer.toString(response));
//                return null;
//            }
//
//            is = conn.getInputStream();
//            result = IOUtils.toByteArray(is);
//
//        } catch (Exception e) {
//            Log.e(Constants.TAG, "Error getting URL using parameters", e);
//            return null;
//        } finally {
//            if (is != null) {
//                IOUtils.closeQuietly(is);
//            }
//        }
//
//        return result;
//    }
//
//    public String getUrl(String stringUrl, HashMap<String, String> params) {
//
//        byte[] result = getUrlBytes(stringUrl, params);
//
//        if (result != null) {
//            String resultString;
//
//            try {
//                resultString = new String(result, "UTF-8");
//                return resultString;
//            } catch (UnsupportedEncodingException e) {
//                //This shouldn't happen
//                Log.e(Constants.TAG, "Could not convert web data to UTF8");
//            }
//        }
//
//        return null;
//    }


    protected HttpURLConnection openConnection(String stringUrl) throws IOException {
        URL url = new URL(stringUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        return conn;
    }

    protected String getParamsString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean firstEntry = true;

        Set<String> keys = params.keySet();
        for (String key: keys) {
            if (firstEntry) {
                firstEntry = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(key), "UTF-8"));
        }

        return result.toString();
    }
}