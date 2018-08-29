package com.mumakid.wifiplugin;

/**
 * Created by ymlsmile on 2017/8/17.
 */
public class WifiElement implements Comparable<WifiElement> {
    private String mac;
    private Byte rssi;

    public WifiElement(String mac, Byte rssi) {
        this.mac = mac;
        this.rssi = rssi;
    }

    public String getMac() {
        return mac;
    }

    public Byte getRssi() {
        return rssi;
    }

    @Override
    public int compareTo(WifiElement o) {
        return this.mac.compareTo(o.mac);
    }

    @Override
    public String toString() {
        return "mac:" + mac + "rssi:" + rssi + '\n';
    }
}
