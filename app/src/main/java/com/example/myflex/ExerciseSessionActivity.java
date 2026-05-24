package com.example.myflex;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myflex.databinding.ActivityExerciseSessionBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExerciseSessionActivity extends AppCompatActivity {

    private static final int    CAMERA_PERMISSION_CODE = 100;
    private static final String TAG                    = "MyFlex";

    private ActivityExerciseSessionBinding binding;
    private ExecutorService cameraExecutor;
    private PoseDetector    poseDetector;
    private final Handler   mainHandler = new Handler(Looper.getMainLooper());

    private int     repCount         = 0;
    private int     targetReps       = 12;
    private String  exerciseName     = "Exercise";
    private boolean isDown           = false;
    private float   currentFormScore = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseSessionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().getStringExtra("exercise_name") != null)
            exerciseName = getIntent().getStringExtra("exercise_name");
        targetReps = getIntent().getIntExtra("target_reps", 12);

        binding.tvAiFeedback.setText(
                "🤖 AI: Starting " + exerciseName + " — Target: " + targetReps + " reps"
        );

        setupPoseDetector();
        cameraExecutor = Executors.newSingleThreadExecutor();
        checkCameraPermission();

        binding.btnEndSession.setOnClickListener(v -> {
            cameraExecutor.shutdown();
            saveAndFinish();
        });
    }

    // ── فحص الإذن ─────────────────────────────────────────────────
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "✅ Camera permission already granted");
            startCamera();
        } else {
            Log.d(TAG, "⚠️ Requesting camera permission...");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE
            );
        }
    }

    // ── إعداد ML Kit ───────────────────────────────────────────────
    private void setupPoseDetector() {
        AccuratePoseDetectorOptions options =
                new AccuratePoseDetectorOptions.Builder()
                        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                        .build();
        poseDetector = PoseDetection.getClient(options);
        Log.d(TAG, "✅ PoseDetector initialized");
    }

    // ── تشغيل الكاميرا ────────────────────────────────────────────
    private void startCamera() {
        Log.d(TAG, "🎥 startCamera() called");

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();
                Log.d(TAG, "✅ CameraProvider ready");

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(
                        binding.cameraPreview.getSurfaceProvider()
                );

                // Image Analysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                // جرب الكاميرا الأمامية أولاً
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis
                );

                Log.d(TAG, "✅ Camera bound to lifecycle");
                mainHandler.post(() ->
                        binding.tvAiFeedback.setText("🤖 AI: Camera ready! Start exercising.")
                );

            } catch (Exception e) {
                Log.e(TAG, "❌ Camera failed: " + e.getMessage());
                e.printStackTrace();

                // إذا الكاميرا الأمامية فشلت جرب الخلفية
                tryBackCamera();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ── جرب الكاميرا الخلفية كبديل ───────────────────────────────
    private void tryBackCamera() {
        Log.d(TAG, "🔄 Trying back camera...");
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = future.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(
                        binding.cameraPreview.getSurfaceProvider()
                );

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                        )
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );

                Log.d(TAG, "✅ Back camera bound");
                mainHandler.post(() ->
                        binding.tvAiFeedback.setText("🤖 AI: Camera ready (back). Start!")
                );

            } catch (Exception e) {
                Log.e(TAG, "❌ Back camera also failed: " + e.getMessage());
                e.printStackTrace();

                mainHandler.post(() ->
                        Toast.makeText(this,
                                "Camera not available: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ── تحليل الفريم ──────────────────────────────────────────────
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(ImageProxy imageProxy) {
        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage inputImage = InputImage.fromMediaImage(
                imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees()
        );

        poseDetector.process(inputImage)
                .addOnSuccessListener(pose -> {
                    binding.poseOverlay.updatePose(
                            pose,
                            imageProxy.getWidth(),
                            imageProxy.getHeight(),
                            true
                    );
                    analyzePoseForExercise(pose);
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Pose detection failed: " + e.getMessage())
                )
                .addOnCompleteListener(task -> imageProxy.close());
    }

    // ── تحليل الحركة ──────────────────────────────────────────────
    private void analyzePoseForExercise(Pose pose) {
        PoseLandmark leftHip      = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee     = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle    = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);

        if (leftHip == null || leftKnee == null || leftAnkle == null) {
            updateUI("🤖 AI: Stand fully in frame...", 0f, false);
            return;
        }

        float kneeAngle = calculateAngle(
                leftHip.getPosition().x,   leftHip.getPosition().y,
                leftKnee.getPosition().x,  leftKnee.getPosition().y,
                leftAnkle.getPosition().x, leftAnkle.getPosition().y
        );

        float backAngle = 170f;
        if (leftShoulder != null) {
            backAngle = calculateAngle(
                    leftShoulder.getPosition().x, leftShoulder.getPosition().y,
                    leftHip.getPosition().x,      leftHip.getPosition().y,
                    leftKnee.getPosition().x,     leftKnee.getPosition().y
            );
        }

        if (kneeAngle < 100 && !isDown) isDown = true;

        if (kneeAngle > 160 && isDown) {
            isDown = false;
            repCount++;
            mainHandler.post(() -> {
                binding.tvRepCount.setText(String.valueOf(repCount));
                if (repCount >= targetReps) {
                    binding.tvAiFeedback.setText(
                            "🎉 All " + targetReps + " reps done!"
                    );
                    binding.btnEndSession.setText("Save & Finish");
                }
            });
        }

        float formScore;
        String msg;

        if (backAngle > 165)      { formScore = 95f; msg = "🤖 Perfect posture!"; }
        else if (backAngle > 145) { formScore = 75f; msg = "🤖 Straighten your back."; }
        else if (backAngle > 120) { formScore = 55f; msg = "⚠️ Back bending — fix it!"; }
        else                      { formScore = 35f; msg = "🚨 Stop! Risky position!"; }

        if (isDown && kneeAngle >= 85 && kneeAngle <= 100) {
            msg = "🤖 Great depth! Hold 1 sec.";
            formScore = Math.max(formScore, 85f);
        }

        currentFormScore = formScore;
        boolean fatigue  = repCount >= (int)(targetReps * 0.8);

        final String finalMsg   = msg;
        final float  finalScore = formScore;
        mainHandler.post(() -> updateUI(finalMsg, finalScore, fatigue));
    }

    // ── تحديث الـ UI ───────────────────────────────────────────────
    private void updateUI(String msg, float formScore, boolean fatigue) {
        binding.tvAiFeedback.setText(msg);
        binding.tvFormScore.setText((int) formScore + "%");

        if (formScore >= 85)      binding.tvFormScore.setTextColor(0xFF34D399);
        else if (formScore >= 60) binding.tvFormScore.setTextColor(0xFFFFA000);
        else                      binding.tvFormScore.setTextColor(0xFFE53935);

        if (fatigue) {
            binding.tvFatigueLevel.setText("High");
            binding.tvFatigueLevel.setTextColor(0xFFE53935);
            binding.tvFatigueAlert.setVisibility(View.VISIBLE);
        } else if (repCount > (int)(targetReps * 0.5)) {
            binding.tvFatigueLevel.setText("Med");
            binding.tvFatigueLevel.setTextColor(0xFFFFA000);
            binding.tvFatigueAlert.setVisibility(View.GONE);
        } else {
            binding.tvFatigueLevel.setText("Low");
            binding.tvFatigueLevel.setTextColor(0xFF34D399);
            binding.tvFatigueAlert.setVisibility(View.GONE);
        }
    }

    // ── حساب الزاوية ───────────────────────────────────────────────
    private float calculateAngle(float ax, float ay,
                                 float bx, float by,
                                 float cx, float cy) {
        double r = Math.atan2(cy - by, cx - bx)
                - Math.atan2(ay - by, ax - bx);
        double a = Math.abs(Math.toDegrees(r));
        if (a > 180) a = 360 - a;
        return (float) a;
    }

    // ── نتيجة طلب الإذن ───────────────────────────────────────────
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Permission granted by user");
                startCamera();
            } else {
                Log.e(TAG, "❌ Permission denied by user");
                Toast.makeText(this,
                        "Camera permission required!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    // ── حفظ والخروج ───────────────────────────────────────────────
    private void saveAndFinish() {
        getSharedPreferences("myflex", MODE_PRIVATE).edit()
                .putInt("last_reps",   repCount)
                .putFloat("last_form", currentFormScore)
                .apply();
        Toast.makeText(this,
                "Saved! Reps: " + repCount + " | Form: " + (int) currentFormScore + "%",
                Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) cameraExecutor.shutdown();
        if (poseDetector   != null) poseDetector.close();
        mainHandler.removeCallbacksAndMessages(null);
    }
}