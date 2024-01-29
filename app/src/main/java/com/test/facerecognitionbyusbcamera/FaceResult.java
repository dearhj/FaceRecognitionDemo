package com.test.facerecognitionbyusbcamera;

import com.rockchip.iva.face.RockIvaFaceInfo;

public class FaceResult {

    private long mDbId = -1;

    private String mName = null;

    private String mFaceFeature = null;

    private RockIvaFaceInfo mFaceinfo = null;

    private float score;

    public FaceResult() { }

    public FaceResult(String name) {
        this.mName = name;
    }

    public FaceResult(String name, long dbIndex) {
        this.mName = name;
        this.mDbId = dbIndex;
    }

    public String getName() {
        return mName;
    }

    public float getScore() {
        return score;
    }

    public void setName(String name, float score) {
        this.mName = name;
        this.score = score;
    }

    public long getDBId() {
        return mDbId;
    }

    public void setDBId(int dbId) {
        this.mDbId = dbId;
    }

    public String getFaceFeature() {
        return mFaceFeature;
    }

    public int setFaceFeature(String faceFeature) {
        return 0;
    }

    public RockIvaFaceInfo getFaceInfo() {
        return mFaceinfo;
    }

    public void setFaceInfo(RockIvaFaceInfo faceInfo) {
        mFaceinfo = faceInfo;
    }
}
