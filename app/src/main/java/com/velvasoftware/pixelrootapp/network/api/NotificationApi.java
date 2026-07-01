package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Notification;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface NotificationApi {
    @GET("notifications")
    Call<List<Notification>> getNotifications();
}
