package com.test.facerecognitionbyusbcamera.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static String readFile(String path) {
        String str = null;
        File jsonFile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(jsonFile);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (fis.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            str = sb.toString();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String readFileFromResRaw(Context context, int resId) {
        String str = null;
        try {
            InputStream is = context.getResources().openRawResource(resId);
            byte[] buffer = new byte[10];
            StringBuilder sb = new StringBuilder();
            while (is.read(buffer) != -1) {
                sb.append(new String(buffer));
                buffer = new byte[10];
            }
            str = sb.toString();
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
