package com.dk.log;

import android.util.Log;

public class DKLog {
    public static boolean logEnable = true;

    public static void enableLog() {
        logEnable = true;
    }

    public static void disableLog() {
        logEnable = false;
    }

    public static DKLogCallback gLogCallback = new DKLogCallback() {
        @Override
        public void onReceiveLogI(String tag, String msg) {
            super.onReceiveLogI(tag, msg);
            Log.i(tag, msg);
        }

        @Override
        public void onReceiveLogD(String tag, String msg) {
            super.onReceiveLogD(tag, msg);
            Log.d(tag, msg);
        }

        @Override
        public void onReceiveLogE(String tag, String msg) {
            super.onReceiveLogE(tag, msg);
            Log.e(tag, msg);
        }
    };

    public static void setLogCallback(DKLogCallback callback) {
        gLogCallback = callback;
    }

    public static void d(String tag, String s) {
        if ( !logEnable ) {
            return;
        }

        if (gLogCallback != null) {
            gLogCallback.onReceiveLogD(tag, s);
        }
    }

    public static void i(String tag, String s) {
        if ( !logEnable ) {
            return;
        }

        if (gLogCallback != null) {
            gLogCallback.onReceiveLogI(tag, s);
        }
    }

    public static void e(String tag, String s) {
        if ( !logEnable ) {
            return;
        }

        if (gLogCallback != null) {
            gLogCallback.onReceiveLogE(tag, s);
        }
    }

    public static void d(String tag, Object s) {
        d(tag, s.toString());
    }

    public static void i(String tag, Object s) {
        i(tag, s.toString());
    }

    public static void e(String tag, Object s) {
        e(tag, s.toString());
    }

    public static void d(String tag, StackTraceElement[] elements) {
        for(StackTraceElement elem : elements) {
            d(tag, elem);
        }
    }

    public static void i(String tag, StackTraceElement[] elements) {
        for(StackTraceElement elem : elements) {
            i(tag, elem);
        }
    }

    public static void e(String tag, StackTraceElement[] elements) {
        for(StackTraceElement elem : elements) {
            e(tag, elem);
        }
    }
}
