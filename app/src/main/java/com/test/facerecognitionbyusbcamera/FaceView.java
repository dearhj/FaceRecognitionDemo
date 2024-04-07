package com.test.facerecognitionbyusbcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by wangjingyi on 28/03/2017.
 */

public class FaceView extends View {
    private List<String> ids;
    private List<String> yaws;
    private List<String> pitchs;
    private List<String> rolls;
    private List<String> blurs;
    private List<String> smiles;
    private List<Rect> rect;
    private Paint paint = new Paint();
    private Paint idPaint = new Paint();
    private Paint posePaint = new Paint();
    private Paint backPaint = new Paint();

    private void initData() {
        ids = new ArrayList<String>();
        yaws = new ArrayList<>();
        pitchs = new ArrayList<>();
        rolls = new ArrayList<>();
        blurs = new ArrayList<>();
        smiles = new ArrayList<>();
        rect = new ArrayList<Rect>();
        paint.setARGB(122, 255, 255, 255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10.0f);

//        backPaint.setARGB(122, 255, 255, 255);
//        backPaint.setStyle(Paint.Style.FILL);
//
//        idPaint.setARGB(255, 80, 80, 80);
//        idPaint.setTextSize(40);
//
//        posePaint.setARGB(255, 80, 80, 80);
//        posePaint.setTextSize(25);
    }

    public FaceView(Context context) {
        super(context);
        initData();
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public void addId(String label) {
        ids.add(label);
    }

    public void addYaw(String label) {
        yaws.add(label);
    }
    public void addPitch(String label) {
        pitchs.add(label);
    }
    public void addRoll(String label) {
        rolls.add(label);
    }
    public void addBlur(String label)  {
        blurs.add(label);
    }
    public void addSmile(String lable) {
        smiles.add(lable);
    }

    public void addRect(RectF rect) {
        Rect buffer = new Rect();
        buffer.left = (int) rect.left;
        buffer.top = (int) rect.top;
        buffer.right = (int) rect.right;
        buffer.bottom = (int) rect.bottom;
        this.rect.add(buffer);
    }

    public void clear() {
        rect.clear();
        ids.clear();
        yaws.clear();
        rolls.clear();
        blurs.clear();
        pitchs.clear();
        smiles.clear();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < rect.size(); i++) {
            Rect r = rect.get(i);
            canvas.drawRect(r, paint);
//            canvas.drawRect(r.right+5, r.top - 5, r.right + ids.get(i).length() * 25, r.top + 200, backPaint);
//            canvas.drawText(ids.get(i), r.right + 5, r.top + 30, idPaint);
//            canvas.drawText(yaws.get(i), r.right + 5, r.top + 60, posePaint);
//            canvas.drawText(pitchs.get(i), r.right + 5, r.top + 93, posePaint);
//            canvas.drawText(rolls.get(i), r.right + 5, r.top + 126, posePaint);
//            canvas.drawText(blurs.get(i), r.right + 5, r.top + 159, posePaint);
//            canvas.drawText(smiles.get(i), r.right + 5, r.top + 192, posePaint);
        }
        this.clear();
    }
}
