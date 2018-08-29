package com.mumakid.wifiplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Demon on 2018/4/18.
 */

public class WifiUtil {

    private static final int PERIOD = 5;
    private static final int LIMIT = 3000;
    private WifiReceiver mWifiReceiver;
    private WifiManager mWifiManager = null;
    private Context appContext;
    private int number;
    private List<List<WifiElement>> records;
    private IWifiResult mIWifiResult;
    private long startTime;
    private boolean isLocation = false;

    public WifiUtil(Context appContext, IWifiResult iWifiResult) {
        super();
        this.records = new ArrayList<List<WifiElement>>();
        this.appContext = appContext;
        this.mIWifiResult = iWifiResult;
        mWifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        this.number = -1;
    }

    public void collect() {
        this.collect(false);
    }

    public void collect(boolean isLocation) {
        this.reset();
        this.isLocation = isLocation;
        startTime = System.currentTimeMillis();
        mWifiManager.startScan();

    }

    private void reset() {
        this.number = 0;
        this.records.clear();
        this.isLocation = false;
        wifiRegister();
    }

    //注册：
    private void wifiRegister() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mWifiReceiver = new WifiReceiver();
        appContext.registerReceiver(mWifiReceiver, filter);
    }

    public void wifiUnregister() {
        if (mWifiReceiver != null) {
            appContext.unregisterReceiver(mWifiReceiver);
        }
    }

    //接收：
    class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("WifiReceiver", action);
            // / Wifi 状态变化
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
                if (number > -1) {
                    handler();
                }
            }
        }

        private synchronized void handler() {
            if (isLocation) {
                List<ScanResult> list = mWifiManager.getScanResults();
                List<WifiElement> record = filter(list);
                wifiUnregister();
                if (mIWifiResult != null) {
                    mIWifiResult.location(record);
                }
            } else {
                number++;
                List<ScanResult> list = mWifiManager.getScanResults();
                List<WifiElement> record = filter(list);
                records.add(record);
                long rightTime = System.currentTimeMillis();
                if (rightTime - startTime > LIMIT) {
                    if (compare()) {
                        records.remove(records.size() - 1);
                    }
                    number = -1;
                    wifiUnregister();
                    if (mIWifiResult != null) {
                        mIWifiResult.callBack(records);
                    }
                } else {
                    if (number == 1) {
                        scan();
                    } else {
                        if (compare()) {
                            records.remove(records.size() - 1);
                            number--;
                            scan();
                        } else {
                            if (number >= PERIOD) {
                                number = -1;
                                wifiUnregister();
                                if (mIWifiResult != null) {
                                    mIWifiResult.callBack(records);
                                }
                            } else {
                                mWifiManager.startScan();
                            }
                        }
                    }
                }
            }
        }

        private boolean compare() {
            if (records.size() > 1) {
                Gson gson = new Gson();
                Collections.sort(records.get(records.size() - 2));
                String a = gson.toJson(records.get(records.size() - 2));
                Collections.sort(records.get(records.size() - 1));
                String b = gson.toJson(records.get(records.size() - 1));
                return a.equals(b);
            }
            return false;
        }

        private List<WifiElement> filter(List<ScanResult> list) {
            List<WifiElement> record = new ArrayList<WifiElement>();
            ///只保留2.4G
            for (ScanResult sr : list) {
                if (sr.frequency > 2400 && sr.frequency < 2500) {
                    WifiElement e = new WifiElement(sr.BSSID.toUpperCase().replaceAll(":", ""), (byte) Math.abs(sr.level));
                    record.add(e);
                }
            }
            return record;
        }

        private void scan() {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    /**
                     *要执行的操作
                     */
                    mWifiManager.startScan();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 500);//500毫秒后执行TimeTask的run方法
        }
    }

    public interface IWifiResult {
        void callBack(List<List<WifiElement>> records);

        void location(List<WifiElement> record);
    }
}

