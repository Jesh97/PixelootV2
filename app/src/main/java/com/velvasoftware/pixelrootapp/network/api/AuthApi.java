package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.network.request.LoginRequest;
import com.velvasoftware.pixelrootapp.network.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);
}
