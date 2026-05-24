package com.example.myflex;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_MS = 2500;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        startAnimations();
        handler.postDelayed(this::decideNavigation, SPLASH_MS);
    }

    private void decideNavigation() {
        // ✅ تأكد من تسجيل الخروج من Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            FirebaseAuth.getInstance().signOut();
        }

        // ✅ مسح الجلسة المحلية بالكامل
        new SessionManager(this).logout();

        // ✅ اذهب دائماً إلى شاشة تسجيل الدخول
        navigateTo(LoginActivity.class);
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ── أنيميشن ───────────────────────────────────────────────────
    private void startAnimations() {
        CardView cardLogo = findViewById(R.id.card_logo);
        if (cardLogo != null) {
            cardLogo.setScaleX(0.4f);
            cardLogo.setScaleY(0.4f);
            animateScale(cardLogo, 150, 520);
        }

        animateSlideView(R.id.tv_app_name,  28f, 530, 400);
        animateSlideView(R.id.tv_tagline,   18f, 730, 360);
        animateSlideView(R.id.pills_row,    14f, 910, 340);
        animateSlideView(R.id.features_row, 14f, 1080, 340);

        fadein(R.id.dots_row,      1280, 480);
        fadein(R.id.tv_compliance, 1280, 480);
        fadein(R.id.home_bar,      1280, 480);
    }

    private void animateScale(View v, long delay, long dur) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(v, View.ALPHA,   0f, 1f),
                ObjectAnimator.ofFloat(v, View.SCALE_X, 0.4f, 1f),
                ObjectAnimator.ofFloat(v, View.SCALE_Y, 0.4f, 1f)
        );
        set.setStartDelay(delay);
        set.setDuration(dur);
        set.start();
    }

    private void animateSlideView(int viewId, float fromY, long delay, long dur) {
        View v = findViewById(viewId);
        if (v == null) return;
        v.setTranslationY(fromY);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(v, View.ALPHA,         0f, 1f),
                ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, fromY, 0f)
        );
        set.setStartDelay(delay);
        set.setDuration(dur);
        set.start();
    }

    private void fadein(int viewId, long delay, long dur) {
        View v = findViewById(viewId);
        if (v == null) return;
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, View.ALPHA, 0f, 1f);
        anim.setStartDelay(delay);
        anim.setDuration(dur);
        anim.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}