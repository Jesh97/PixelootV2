package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.request.ForgotPasswordRequest;
import com.velvasoftware.pixelrootapp.network.request.LoginRequest;
import com.velvasoftware.pixelrootapp.network.request.RegisterRequest;
import com.velvasoftware.pixelrootapp.network.request.ResetPasswordRequest;
import com.velvasoftware.pixelrootapp.network.request.VerifyResetCodeRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    // BASE_URL ya incluye ".../api/", y el blueprint está montado en url_prefix='/api/auth'
    @POST("auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest loginRequest);

    // Registro público: el backend siempre crea el usuario con rol_id = 1 (CLIENTE)
    @POST("auth/register")
    Call<ApiResponse<User>> register(@Body RegisterRequest registerRequest);

    // Envía un código de 6 dígitos por correo, válido 15 minutos
    @POST("auth/forgot-password")
    Call<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);

    // Valida el código antes de dejar avanzar a la pantalla de nueva contraseña
    @POST("auth/verify-reset-code")
    Call<ApiResponse<Void>> verifyResetCode(@Body VerifyResetCodeRequest request);

    // Valida el código otra vez y actualiza la contraseña
    @POST("auth/reset-password")
    Call<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest request);
}