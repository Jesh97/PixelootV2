package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.velvasoftware.pixelrootapp.databinding.ActivityMainBinding;
import com.velvasoftware.pixelrootapp.network.SessionManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupLottie();
        navigateToLogin();
    }

    private void setupLottie() {
        binding.lottAnimationViewApp.setAnimation("joystick.lottie");
        binding.lottAnimationViewApp.playAnimation();
        binding.lottAnimationViewApp.setRepeatCount(3);
    }

    private void navigateToLogin() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = SessionManager.getInstance(this);
            boolean skipLogin = session.isLoggedIn() && session.isRememberMe();

            Intent intent = new Intent(MainActivity.this, skipLogin ? MenuActivity.class : LoginActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}