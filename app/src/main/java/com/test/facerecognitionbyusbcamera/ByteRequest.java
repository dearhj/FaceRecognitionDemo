package com.test.facerecognitionbyusbcamera;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Created by xingchaolei on 2017/12/6.
 */

public class ByteRequest extends Request<byte[]> {

    private Response.Listener<byte[]> mListener;

    public ByteRequest(int method, String url, Response.Listener<byte[]> successListener, Response.ErrorListener listener) {
        super(method, url, listener);
        mListener = successListener;
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(byte[] response) {
        mListener.onResponse(response);
    }

}
