package com.rockchip.iva.face;

import android.util.Base64;

public class RockIvaFaceFeature {
    public byte[] data;

    public RockIvaFaceFeature(String feature) {
        data = Base64.decode(feature, Base64.DEFAULT);
    }
}
