package com.example.myflex;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class PoseOverlayView extends View {

    private Pose    pose        = null;
    private int     imageWidth  = 1;
    private int     imageHeight = 1;
    private boolean isFrontCamera = true;

    private final Paint dotPaint  = new Paint();
    private final Paint linePaint = new Paint();

    public PoseOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);

        dotPaint.setColor(Color.WHITE);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setAntiAlias(true);

        linePaint.setColor(0xFF90CAF9);
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
    }

    public void updatePose(Pose pose, int imgW, int imgH, boolean front) {
        this.pose          = pose;
        this.imageWidth    = imgW;
        this.imageHeight   = imgH;
        this.isFrontCamera = front;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pose == null) return;

        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) return;

        // ارسم النقاط
        for (PoseLandmark lm : landmarks) {
            float x = translateX(lm.getPosition().x);
            float y = translateY(lm.getPosition().y);
            canvas.drawCircle(x, y, 10f, dotPaint);
        }

        // ارسم الهيكل
        drawLine(canvas, PoseLandmark.LEFT_SHOULDER,  PoseLandmark.RIGHT_SHOULDER);
        drawLine(canvas, PoseLandmark.LEFT_SHOULDER,  PoseLandmark.LEFT_ELBOW);
        drawLine(canvas, PoseLandmark.LEFT_ELBOW,     PoseLandmark.LEFT_WRIST);
        drawLine(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW);
        drawLine(canvas, PoseLandmark.RIGHT_ELBOW,    PoseLandmark.RIGHT_WRIST);
        drawLine(canvas, PoseLandmark.LEFT_SHOULDER,  PoseLandmark.LEFT_HIP);
        drawLine(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP);
        drawLine(canvas, PoseLandmark.LEFT_HIP,       PoseLandmark.RIGHT_HIP);
        drawLine(canvas, PoseLandmark.LEFT_HIP,       PoseLandmark.LEFT_KNEE);
        drawLine(canvas, PoseLandmark.LEFT_KNEE,      PoseLandmark.LEFT_ANKLE);
        drawLine(canvas, PoseLandmark.RIGHT_HIP,      PoseLandmark.RIGHT_KNEE);
        drawLine(canvas, PoseLandmark.RIGHT_KNEE,     PoseLandmark.RIGHT_ANKLE);
    }

    private void drawLine(Canvas canvas, int startType, int endType) {
        PoseLandmark start = pose.getPoseLandmark(startType);
        PoseLandmark end   = pose.getPoseLandmark(endType);
        if (start == null || end == null) return;
        canvas.drawLine(
                translateX(start.getPosition().x),
                translateY(start.getPosition().y),
                translateX(end.getPosition().x),
                translateY(end.getPosition().y),
                linePaint
        );
    }

    private float translateX(float x) {
        float scale = (float) getWidth() / imageWidth;
        return isFrontCamera ? getWidth() - (x * scale) : x * scale;
    }

    private float translateY(float y) {
        return y * ((float) getHeight() / imageHeight);
    }
}