package com.dk.usbNfc.Card;

import android.util.Log;

import com.dk.log.DKLog;
import com.dk.usbNfc.Card.CpuCard;
import com.dk.usbNfc.Card.Mifare;
import com.dk.usbNfc.DeviceManager.DeviceManager;
import com.dk.usbNfc.DeviceManager.UsbNfcDevice;
import com.dk.usbNfc.Exception.CardNoResponseException;
import com.dk.usbNfc.Tool.StringTool;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class YCTCard {
    private static final String TAG = "YCTCard";

    public YCTCard() {
    }

    static public String getYctCardInfo(UsbNfcDevice uartNfcDevice, int cardType) {
        String errcode = "000000";
        String errmsg = "";

        switch (cardType) {
            case DeviceManager.CARD_TYPE_ISO4443_A:   //寻到A CPU卡
                final CpuCard cpuCard = (CpuCard) uartNfcDevice.getCard();
                if (cpuCard != null) {
                    try {
                        JSONObject data = new JSONObject();
                        JSONObject json = new JSONObject();
                        json.put("errcode", errcode);
                        json.put("errmsg", errmsg);

                        //羊城通
                        String rsp = cpuCard.transceive("00A40400085943542E55534552", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            rsp = cpuCard.transceive("00A40400085041592E41505059 ", 1000);
                            if ( (rsp == null) || !rsp.contains("9000") ) {
                                cpuCard.transceive("00A40000023F00", 1000);

                                rsp = cpuCard.transceive("00a404000f7378312E73682EC9E7BBE1B1A3D5CF ", 1000);
                                if ( (rsp == null) || !rsp.contains("9000") ) {
                                    errcode = "100122";
                                    errmsg = "STATE_CARD_GOTO_OUT";
                                    break;
                                }

                                rsp = cpuCard.transceive("00a4020002EF0500 ", 1000);
                                if ( (rsp == null) || !rsp.contains("9000") ) {
                                    errcode = "100122";
                                    errmsg = "STATE_CARD_GOTO_OUT";
                                    break;
                                }

                                DKLog.d(TAG, "指令3-发送：00b2010000 ");
                                rsp = cpuCard.transceive("00b2010000", 1000);
                                if ( (rsp == null) || !rsp.contains("9000") ) {
                                    errcode = "100123";
                                    errmsg = "STATE_CARD_GOTO_OUT";
                                    break;
                                }
                                DKLog.d(TAG, "返回：" + rsp);
                                data.put("aaz501", rsp);

                                DKLog.d(TAG, "指令3-发送：00b2070000 ");
                                rsp = cpuCard.transceive("00b2070000", 1000);
                                if ( (rsp == null) || !rsp.contains("9000") ) {
                                    errcode = "100123";
                                    errmsg = "STATE_CARD_GOTO_OUT";
                                    break;
                                }
                                DKLog.d(TAG, "返回：" + rsp);
                                data.put("aaz500", rsp);

                                data.put("cardtype", "3");
                                json.put("data", data);

                                return json.toString();
                            }
                        }

                        DKLog.d(TAG, "指令3-发送：C4FE000000 ");
                        rsp = cpuCard.transceive("C4FE000000", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            errcode = "100123";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        DKLog.d(TAG, "返回：" + rsp);
                        if (rsp.length() < 18) {
                            errcode = "100123";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        data.put("fno", rsp.substring(0, 16));

                        DKLog.d(TAG, "指令4-发送：00A4000002DDF1");
                        rsp = cpuCard.transceive("00A4000002DDF1", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            errcode = "100124";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        DKLog.d(TAG, "返回：" + rsp);

                        DKLog.d(TAG, "指令5-发送：00B0950000");
                        rsp = cpuCard.transceive("00B0950000", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            errcode = "100125";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        DKLog.d(TAG, "返回：" + rsp);
                        if (rsp.length() < 122) {
                            errcode = "100125";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        data.put("lno", rsp.substring(16, 32));
                        data.put("city", rsp.substring(98, 100));
                        data.put("specardtype", rsp.substring(116, 120));

                        DKLog.d(TAG, "指令6-发送：00A4000002ADF3");
                        rsp = cpuCard.transceive("00A4000002ADF3", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            errcode = "100126";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        DKLog.d(TAG, "返回：" + rsp);

                        DKLog.d(TAG, "指令7-发送：0020000003123456");
                        rsp = cpuCard.transceive("0020000003123456", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            errcode = "100127";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        DKLog.d(TAG, "返回：" + rsp);

                        DKLog.d(TAG, "指令8-发送：805000020B0100000000000000000000");
                        rsp = cpuCard.transceive("805000020B0100000000000000000000", 1000);
                        if ( (rsp == null) || !rsp.contains("9000") ) {
                            errcode = "100128";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        DKLog.d(TAG, "返回：" + rsp);
                        if (rsp.length() < 4) {
                            errcode = "100128";
                            errmsg = "STATE_CARD_GOTO_OUT";
                            break;
                        }
                        data.put("traninfo", rsp.substring(0, rsp.length() - 4));
                        data.put("cardtype", "2");
                        data.put("amount", "00000000");

                        SimpleDateFormat format= new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                        String myDate = format.format(new Date());
                        data.put("trantime", myDate);

                        json.put("data", data);

                        return json.toString();
                    } catch (CardNoResponseException | JSONException e) {
                        errcode = "100122";
                        errmsg = "STATE_CARD_GOTO_OUT";
                        DKLog.e(TAG, e.getMessage());
                        DKLog.e(TAG, e.getStackTrace());
                    }
                }
                break;
            case DeviceManager.CARD_TYPE_MIFARE:   //寻到Mifare卡
                final Mifare mifare = (Mifare) uartNfcDevice.getCard();
                if (mifare != null) {
                    try {

                        int cnt = 0;
                        //配置密钥到NFC模块，此密钥在读取时会用到
                        byte[][] keys = {
                                {(byte) 0xEE, (byte) 0x9B, (byte) 0xD3, (byte) 0x61, (byte) 0xB0, (byte) 0x1B},
                                {(byte) 0xE2, (byte) 0xEB, (byte) 0x2C, (byte) 0x59, (byte) 0xA3, (byte) 0xD6}
                        };

                        do {
                            //验证密钥
                            boolean anth = mifare.authenticate((byte) 4, Mifare.MIFARE_KEY_TYPE_A, keys[cnt++]);
                            if (!anth) {
                                errcode = "100121";
                                errmsg = "ERRO_CARD_M1_READ_TAG";
                                continue;
                            }

                            errcode = "000000";
                            errmsg = "";

                            JSONObject data = new JSONObject();
                            data.put("cardtype", "1");
                            data.put("city", "00");
                            data.put("fno", StringTool.byteHexToSting(mifare.uid));
                            data.put("specardtype", "");

                            JSONObject json = new JSONObject();
                            json.put("errcode", errcode);
                            json.put("errmsg", errmsg);

                            byte[] rspBytes = mifare.read((byte) 4);
                            if (rspBytes == null || rspBytes.length != 16) {
                                errcode = "100121";
                                errmsg = "ERRO_CARD_M1_READ_TAG";
                            }
                            else {
                                String rsp = StringTool.byteHexToSting(rspBytes);
                                DKLog.d(TAG, "块4数据：" + rsp);
                                data.put("lno", rsp.substring(0, 8) + rsp.substring(10, 18));

                                json.put("data", data);

                                return json.toString();
                            }
                        } while (cnt < keys.length);
                    } catch (CardNoResponseException | JSONException e) {
                        errcode = "100121";
                        errmsg = "ERRO_CARD_M1_READ_TAG";
                        DKLog.e(TAG, e.getMessage());
                        DKLog.e(TAG, e.getStackTrace());
                    }
                }
                break;

            default:
                break;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("data", "");
            json.put("errcode", errcode);
            json.put("errmsg", errmsg);
        } catch (JSONException e) {
            DKLog.e(TAG, e.getMessage());
            DKLog.e(TAG, e.getStackTrace());
        }

        return json.toString();
    }
}
