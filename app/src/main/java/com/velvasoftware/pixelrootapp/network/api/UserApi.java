package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.SavedCard;
import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.request.FcmTokenRequest;
import com.velvasoftware.pixelrootapp.network.request.SaveCardRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApi {
    // GET /api/usuarios/perfil (requiere JWT, ya lo agrega RetrofitClient automáticamente)
    @GET("usuarios/perfil")
    Call<ApiResponse<User>> getProfile();

    // PUT /api/usuarios/perfil
    @PUT("usuarios/perfil")
    Call<ApiResponse<User>> updateProfile(@Body User user);

    // POST /api/usuarios/tarjetas -> guarda solo últimos 4 + marca + expiración (nunca el número completo)
    @POST("usuarios/tarjetas")
    Call<ApiResponse<List<SavedCard>>> saveCard(@Body SaveCardRequest request);

    // GET /api/usuarios/tarjetas
    @GET("usuarios/tarjetas")
    Call<ApiResponse<List<SavedCard>>> getSavedCards();

    // DELETE /api/usuarios/tarjetas/{id}
    @DELETE("usuarios/tarjetas/{id}")
    Call<ApiResponse<Void>> deleteCard(@Path("id") int cardId);

    // PUT /api/usuarios/fcm-token — registra el token FCM del dispositivo actual
    @PUT("usuarios/fcm-token")
    Call<ApiResponse<Void>> registrarFcmToken(@Body FcmTokenRequest request);
}