package com.dk.usbNfc.Card;

import com.dk.usbNfc.DeviceManager.DeviceManager;

public class I125KCard extends Card{
    public I125KCard(DeviceManager deviceManager) {
        super(deviceManager);
    }
    public  I125KCard(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }
}
