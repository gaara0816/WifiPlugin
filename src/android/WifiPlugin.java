package com.mumakid.wifiplugin;

import android.Manifest;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * This class echoes a string called from JavaScript.
 */
public class WifiPlugin extends CordovaPlugin {

    public static String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * Gzip压缩wifi采集数据
     *
     * @param unGzipStr
     * @return
     */
    public String compressForGzip(String unGzipStr) {

        if (TextUtils.isEmpty(unGzipStr)) {
            return null;
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(baos);
            gzip.write(unGzipStr.getBytes("UTF-8"));
            gzip.close();
            byte[] encode = baos.toByteArray();
            baos.flush();
            baos.close();
            return Base64Encoder.encode(encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hasPermissions() {
        for (String p : permissions) {
            if (!PermissionHelper.hasPermission(this, p)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        Log.d("WifiPlugin", "enter execute");
        if (!this.hasPermissions()) {
            PermissionHelper.requestPermissions(this, 0, permissions);
            return true;
        } else if (action.equals("wifiLocation")) {
            this.wifiLocation(callbackContext);
            return true;
        } else if (action.equals("wifiCollection")) {
            WifiUtil util = new WifiUtil(this.cordova.getActivity().getApplicationContext(), new WifiUtil.IWifiResult() {
                @Override
                public void callBack(List<List<WifiElement>> records) {
                    String base64Records = "";
                    try {
                        String recordsListStr = new Gson().toJson(records);
                        base64Records = compressForGzip(recordsListStr);
                        Log.d("XMM", "base64Records------->" + base64Records);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(base64Records);
                }

                @Override
                public void location(List<WifiElement> record) {
                    String base64Records = "";
                    try {
                        String recordsListStr = new Gson().toJson(record);
                        base64Records = compressForGzip(recordsListStr);
                        Log.d("XMM", "base64Records------->" + base64Records);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(base64Records);
                }
            });
            util.collect();
            return true;
        }
        return false;
    }

    private void wifiLocation(final CallbackContext callbackContext) {
        WifiUtil util = new WifiUtil(this.cordova.getActivity().getApplicationContext(), new WifiUtil.IWifiResult() {
                @Override
                public void callBack(List<List<WifiElement>> records) {
                    String base64Records = "";
                    try {
                        String recordsListStr = new Gson().toJson(records);
                        base64Records = compressForGzip(recordsListStr);
                        Log.d("XMM", "base64Records------->" + base64Records);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(base64Records);
                }

                @Override
                public void location(List<WifiElement> record) {
                    String base64Records = "";
                    try {
                        String recordsListStr = new Gson().toJson(record);
                        base64Records = compressForGzip(recordsListStr);
                        Log.d("XMM", "base64Records------->" + base64Records);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    callbackContext.success(base64Records);
                }
            });
        util.collect(true);
    }

    private void wifiCollection(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
}

