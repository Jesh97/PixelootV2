package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Notification;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotificationApi {
    // BASE_URL ya incluye ".../api/", y el blueprint está montado en url_prefix='/api/notificaciones'
    @GET("notificaciones/")
    Call<ApiResponse<List<Notification>>> getNotifications();

    @PUT("notificaciones/{id}/marcar_leido")
    Call<ApiResponse<Void>> markAsRead(@Path("id") int notificationId);
}