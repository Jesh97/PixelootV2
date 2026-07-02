package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.velvasoftware.pixelrootapp.databinding.ActivityResetPasswordBinding;
import com.velvasoftware.pixelrootapp.network.api.AuthApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.ResetPasswordRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private String correo;
    private String codigo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        correo = getIntent().getStringExtra("correo");
        codigo = getIntent().getStringExtra("codigo");

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        binding.btnUpdatePassword.setOnClickListener(v -> attemptUpdatePassword());
    }

    private void attemptUpdatePassword() {
        if (correo == null || codigo == null) {
            Toast.makeText(this, "Sesión de recuperación inválida, vuelve a intentarlo desde el inicio", Toast.LENGTH_LONG).show();
            return;
        }

        String nueva = binding.newPasswordEditText.getText().toString().trim();
        String confirmacion = binding.confirmPasswordEditText.getText().toString().trim();

        if (nueva.isEmpty() || confirmacion.isEmpty()) {
            Toast.makeText(this, "Completa ambos campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nueva.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!nueva.equals(confirmacion)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnUpdatePassword.setEnabled(false);

        AuthApi authApi = RetrofitClient.getAuthApi();
        authApi.resetPassword(new ResetPasswordRequest(correo, codigo, nueva)).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                binding.btnUpdatePassword.setEnabled(true);
                ApiResponse<Void> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus()) {
                    Toast.makeText(ResetPasswordActivity.this, "Contraseña actualizada, inicia sesión", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String message = body != null ? body.getMessage() : "No se pudo actualizar la contraseña";
                    Toast.makeText(ResetPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                binding.btnUpdatePassword.setEnabled(true);
                Toast.makeText(ResetPasswordActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}