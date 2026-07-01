package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserApi {
    // GET /api/usuarios/perfil (requiere JWT, ya lo agrega RetrofitClient automáticamente)
    @GET("usuarios/perfil")
    Call<ApiResponse<User>> getProfile();

    // PUT /api/usuarios/perfil
    @PUT("usuarios/perfil")
    Call<ApiResponse<User>> updateProfile(@Body User user);
}