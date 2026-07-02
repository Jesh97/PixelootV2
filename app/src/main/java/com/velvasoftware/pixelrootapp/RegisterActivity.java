package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.velvasoftware.pixelrootapp.databinding.ActivityRegisterBinding;
import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.SessionManager;
import com.velvasoftware.pixelrootapp.network.api.AuthApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.RegisterRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupListeners();
    }

    private void setupListeners() {
        binding.signInLinkText.setOnClickListener(v -> finish());

        binding.signUpButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String nombre     = binding.firstNameEditText.getText().toString().trim();
        String apellido   = binding.lastNameEditText.getText().toString().trim();
        String dni        = binding.dniEditText.getText().toString().trim();
        String correo     = binding.emailEditText.getText().toString().trim();
        String contrasena = binding.passwordEditText.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || dni.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dni.length() != 8) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasena.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.signUpButton.setEnabled(false);

        // El backend siempre crea la cuenta con rol_id = 1 (CLIENTE); este formulario
        // es exclusivamente para el registro de clientes desde la app móvil.
        AuthApi authApi = RetrofitClient.getAuthApi();
        authApi.register(new RegisterRequest(nombre, apellido, dni, correo, contrasena))
                .enqueue(new Callback<ApiResponse<User>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                        binding.signUpButton.setEnabled(true);

                        ApiResponse<User> body = response.body();

                        if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                            User user = body.getData();

                            // Auto-login: guardamos la sesión igual que en LoginActivity.
                            SessionManager.getInstance(RegisterActivity.this).saveSession(
                                    user.getToken(),
                                    user.getId(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getEmail(),
                                    user.getRolId(),
                                    true
                            );

                            Toast.makeText(RegisterActivity.this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, MenuActivity.class));
                            finish();
                        } else {
                            String message = body != null ? body.getMessage() : "No se pudo completar el registro";
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                        binding.signUpButton.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}