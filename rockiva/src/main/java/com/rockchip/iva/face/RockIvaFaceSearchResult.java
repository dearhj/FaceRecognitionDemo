package com.rockchip.iva.face;

public class RockIvaFaceSearchResult {
    public float score;
    public String faceId;

    RockIvaFaceSearchResult(String faceId, float score) {
        this.faceId = faceId;
        this.score = score;
    }
}
