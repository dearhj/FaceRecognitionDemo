package com.zaz.zazjni;

public class ZAZJni {
    public native int ZACompareC2CSTemplates(byte[]  stemplate,byte[] ftemplate);
    static {
        System.loadLibrary("ZAFpr");
    }
}

