package com.dk.usbNfc.Card;

import com.dk.usbNfc.DeviceManager.DeviceManager;

public class Topaz extends Card{
    public Topaz(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Topaz(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }


}
