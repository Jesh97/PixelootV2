package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.request.LoginRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    // BASE_URL ya incluye ".../api/", y el blueprint está montado en url_prefix='/api/auth'
    @POST("auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest loginRequest);
}