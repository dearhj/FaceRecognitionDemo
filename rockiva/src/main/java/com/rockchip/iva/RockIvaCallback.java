package com.rockchip.iva;

import java.util.List;

public interface RockIvaCallback {

    public void onResultCallback(String result, int execureState);

    public void onReleaseCallback(List<RockIvaImage> images);
}
