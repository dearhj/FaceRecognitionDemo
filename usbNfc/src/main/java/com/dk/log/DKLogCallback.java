package com.dk.log;

/**
 * Created by lochy on 16/1/19.
 */
public abstract class DKLogCallback {
    public void onReceiveLogI(String tag, String msg) {}
    public void onReceiveLogD(String tag, String msg) {}
    public void onReceiveLogE(String tag, String msg) {}
}
