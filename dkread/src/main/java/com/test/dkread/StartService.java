package com.test.dkread;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.dk.usbNfc.DeviceManager.UsbNfcDevice;

public class StartService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //在进行二次开发时，请注意，当你打开了自动寻卡功能时，但因为页面销毁或服务结束等情况导致DeviceManagerCallback()回调无法接收数据，usbNfcDevice实例被销毁时，
    //在此种情况下去识读卡片，会导致读卡器功耗剧增，发热量增加。建议当你不需要读卡时，调用usbNfcDevice.stoptAutoSearchCard()接口关闭自动寻卡功能，此时模块将进入低功耗模式。
    //此外，为了避免上面所说的DeviceManagerCallback()回调无法接收数据，usbNfcDevice实例被销毁导致功耗增加的情况，建议将相关的读卡识别逻辑写在后台服务中，
    //尽量避免将此部分逻辑与页面UI相关联，并且确保后台服务结束时，关闭自动寻卡功能。

    //此服务的作用是：在设备刚开机时，关闭模块的自动寻卡功能，降低功耗，减少发热，只有当进入模块测试页面，点击打开自动寻卡按钮后，才能识读卡片。
    //当退出模块测试页面，自动寻卡功能又将自动关闭。
    @Override
    public void onCreate() {
        UsbNfcDevice usbNfcDevice;
        usbNfcDevice = new UsbNfcDevice(this);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                usbNfcDevice.stoptAutoSearchCard();
                System.out.println("自动寻卡已关闭。");
                usbNfcDevice.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}