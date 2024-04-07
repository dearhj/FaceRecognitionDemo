package com.test.facerecognitionbyusbcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mcv.facepass.FacePassException;
import mcv.facepass.types.FacePassTrackedFace;

public class ViewUtil {

    private static Toast mRecoToast;
    public static void showFacePassFaceByUvc(FacePassTrackedFace[] detectResult, FaceView faceView) {
        faceView.clear();
        for (FacePassTrackedFace face : detectResult) {
            Log.d("facefacelist", "width " + (face.rect.right - face.rect.left) + " height " + (face.rect.bottom - face.rect.top));
            boolean mirror = false; /* 前摄像头时mirror为true */
            StringBuilder faceIdString = new StringBuilder();
            faceIdString.append("ID = ").append(face.trackId);
            SpannableString faceViewString = new SpannableString(faceIdString);
            faceViewString.setSpan(new TypefaceSpan("fonts/kai"), 0, faceViewString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            String faceRollString = "旋转: " + (int) face.quality.pose.roll + "°";
            String facePitchString = "上下: " + (int) face.quality.pose.pitch + "°";
            String faceYawString = "左右: " + (int) face.quality.pose.yaw + "°";
            String faceBlurString = "模糊: " + face.quality.blur;
            String smileString = "";
            Matrix mat = new Matrix();
            int w = faceView.getMeasuredWidth();
            int h = faceView.getMeasuredHeight();

            int cameraHeight = 720;
            int cameraWidth = 1280;

            float left = 0;
            float top = 0;
            float right = 0;
            float bottom = 0;

            left = face.rect.left;
            top = face.rect.top;
            right = face.rect.right;
            bottom = face.rect.bottom;
            mat.setScale(mirror ? -1 : 1, 1);
            mat.postTranslate(mirror ? (float) cameraWidth : 0f, 0f);
            mat.postScale((float) w / (float) cameraWidth, (float) h / (float) cameraHeight);

            RectF drect = new RectF();
            RectF srect = new RectF(left, top, right, bottom);

            mat.mapRect(drect, srect);
            faceView.addRect(drect);
//            faceView.addId(faceIdString.toString());
//            faceView.addRoll(faceRollString);
//            faceView.addPitch(facePitchString);
//            faceView.addYaw(faceYawString);
//            faceView.addBlur(faceBlurString);
//            faceView.addSmile(smileString);
        }
        faceView.invalidate();
    }


    public static void getFaceImageByFaceTokenByUvc(Context context, Activity activity, String faceToken) {
        if (TextUtils.isEmpty(faceToken)) return;
        try {
            String tips;
            final Bitmap bitmap = ManageActivity.mFacePassHandler.getFaceImage(faceToken.getBytes());
            List<String> name = ManageActivity.dao.selectNameByFaceToken(faceToken);
            if(name.size() != 0) tips = name.get(0);
            else tips = "未知";
            showResultToast(context, activity, "姓名：" + tips, Toast.LENGTH_SHORT, true, bitmap);
        } catch (FacePassException e) {
            e.printStackTrace();
        }
    }

    public static void showResultToast(Context context, Activity activity, CharSequence text, int duration, boolean isSuccess, Bitmap bitmap) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View toastView = inflater.inflate(R.layout.toast, null);
        LinearLayout toastLLayout = (LinearLayout) toastView.findViewById(R.id.toastll);
        if (toastLLayout == null) {
            return;
        }
        toastLLayout.getBackground().setAlpha(100);
        ImageView imageView = (ImageView) toastView.findViewById(R.id.toastImageView);
        TextView idTextView = (TextView) toastView.findViewById(R.id.toastTextView);
        TextView stateView = (TextView) toastView.findViewById(R.id.toastState);
        SpannableString s;
        if (isSuccess) {
            s = new SpannableString("验证成功");
            imageView.setImageResource(R.drawable.success);
        } else {
            s = new SpannableString("验证失败");
            imageView.setImageResource(R.drawable.success);
        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        stateView.setText(s);
        idTextView.setText(text);

        if (mRecoToast == null) {
            mRecoToast = new Toast(context.getApplicationContext());
            mRecoToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        }
        mRecoToast.setDuration(duration);
        mRecoToast.setView(toastView);

        mRecoToast.show();
    }
}
