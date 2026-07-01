package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Order;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OrderApi {
    @GET("orders")
    Call<List<Order>> getOrders();

    @GET("orders/{id}")
    Call<Order> getOrderDetail(@Path("id") String orderId);
}
