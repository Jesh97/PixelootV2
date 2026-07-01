package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.velvasoftware.pixelrootapp.databinding.ActivityLoginBinding;
import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.SessionManager;
import com.velvasoftware.pixelrootapp.network.api.AuthApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.LoginRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.loginActivityPixel, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        binding.createAccountText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        binding.forgotPasswordText.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );

        binding.signInButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String correo = binding.emailEditText.getText().toString().trim();
        String contrasena = binding.passwordEditText.getText().toString().trim();

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.signInButton.setEnabled(false);

        AuthApi authApi = RetrofitClient.getAuthApi();
        authApi.login(new LoginRequest(correo, contrasena)).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                binding.signInButton.setEnabled(true);

                ApiResponse<User> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    User user = body.getData();

                    SessionManager.getInstance(LoginActivity.this).saveSession(
                            user.getToken(),
                            user.getId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail(),
                            user.getRolId()
                    );

                    startActivity(new Intent(LoginActivity.this, MenuActivity.class));
                    finish();
                } else {
                    String message = body != null ? body.getMessage() : "Credenciales inválidas";
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                binding.signInButton.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}