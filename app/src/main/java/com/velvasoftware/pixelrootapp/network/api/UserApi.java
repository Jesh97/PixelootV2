package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface UserApi {
    @GET("profile")
    Call<User> getProfile();

    @PUT("profile")
    Call<User> updateProfile(@Body User user);
}
