package com.rockchip.iva.face;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class RockIvaFaceLibrary {

    public static class FaceRecord {
        public String faceId;
        public byte[] feature;
        public String faceInfo;

        public FaceRecord() {
        }

        public FaceRecord(String faceId, String feature) {
            RockIvaFaceFeature f = new RockIvaFaceFeature(feature);
            this.setValues(faceId, f);
        }

        public FaceRecord(String faceId, RockIvaFaceFeature feature) {
            this.setValues(faceId, feature);
        }

        public void setValues(String faceId, RockIvaFaceFeature feature) {
            this.faceId = faceId;
            this.feature = feature.data;
        }

        public void setValues(String faceId, RockIvaFaceFeature feature, String faceInfo) {
            this.faceId = faceId;
            this.feature = feature.data;
            this.faceInfo = faceInfo;
        }
    }

    public RockIvaFaceLibrary() {

    }

    public int init(String dbDirPath, String libName) {
        this.dbDirPath = dbDirPath;
        File dbDir = new File(dbDirPath);
        if (!dbDir.exists()) {
            boolean ret = dbDir.mkdirs();
            if (ret == false) {
                Log.e("iva", "create db dir fail " + dbDirPath);
                return -1;
            }
        }

        this.libName = libName;
        String dbPath = String.format("%s/%s", dbDirPath, DATABASE_FILENAME);

        long handle = initFacelibrary(dbPath, libName);
        if (handle == -1) {
            return -1;
        }
        this.handle = handle;
        return 0;
    }

    public int release() {
        if (handle == 0) {
            Log.e(TAG, "need init first");
            return -1;
        }
        int ret = clearAll();
        if (ret != 0) {
            return -1;
        }
        return releaseFacelibrary(handle);
    }

    public int addFace(FaceRecord faceRecord) {
        if (handle == 0 || libName == null) {
            Log.e(TAG, "need init first");
            return -1;
        }
        return addFace(handle, libName, faceRecord);
    }

    public int update(String faceId, RockIvaFaceFeature feature) {
        if (handle == 0 || libName == null) {
            Log.e(TAG, "need init first");
            return -1;
        }
        FaceRecord faceRecord = new FaceRecord();
        faceRecord.setValues(faceId, feature);
        return updateFace(handle, libName, faceRecord);
    }

    public int delete(String faceId) {
        if (handle == 0 || libName == null) {
            Log.e(TAG, "need init first");
            return -1;
        }
        return deleteFace(handle, libName, faceId);
    }

    public int clearAll() {
        if (handle == 0 || libName == null) {
            Log.e(TAG, "need init first");
            return -1;
        }
        return clearAll(handle, libName);
    }

    public ArrayList<RockIvaFaceSearchResult> search(RockIvaFaceFeature feature, int topK) {
        ArrayList<RockIvaFaceSearchResult> results = new ArrayList<RockIvaFaceSearchResult>();
        int ret= searchFaceLibrary(handle, libName, feature.data, topK, results);
        if (ret < 0) {
            return null;
        }
        return results;
    }

    private final static String DATABASE_FILENAME = "face.db";
    private final static String TAG = "iva-face";
    private long handle;
    private String dbDirPath;
    private String libName;

    static {
        System.loadLibrary("rockxjni");
    }

    private native long initFacelibrary(String dbPath, String libName);
    private native int releaseFacelibrary(long handle);
    private native int addFace(long handle, String libName, FaceRecord faceRecord);
    private native int deleteFace(long handle, String libName, String faceId);
    private native int updateFace(long handle, String libName, FaceRecord faceRecord);
    private native int clearAll(long handle, String libName);
    private native int searchFaceLibrary(long handle, String libName, byte[] feature, int topK, ArrayList<RockIvaFaceSearchResult> searchResults);
}
