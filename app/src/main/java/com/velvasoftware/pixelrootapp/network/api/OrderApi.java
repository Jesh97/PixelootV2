package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.network.request.AgentConfirmRequest;
import com.velvasoftware.pixelrootapp.network.request.ConfirmOrderRequest;
import com.velvasoftware.pixelrootapp.network.request.AgentConfirmRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderApi {

    // POST /api/pedidos/confirmar_agente -> un agente/admin confirma el pedido de un cliente
    // (PENDIENTE -> COMPLETADO) y el backend descuenta el stock real de cada juego.
    @POST("pedidos/confirmar_agente")
    Call<ApiResponse<Void>> confirmarPedidoAgente(@Body AgentConfirmRequest request);

    // POST /api/pedidos/confirmar -> convierte el carrito (estado='CARRITO') en un pedido real
    @POST("pedidos/confirmar")
    Call<ApiResponse<Order>> confirmarPedido(@Body ConfirmOrderRequest request);

    // GET /api/pedidos/
    @GET("pedidos/")
    Call<ApiResponse<List<Order>>> getOrders();

    // GET /api/pedidos/{id}
    @GET("pedidos/{id}")
    Call<ApiResponse<Order>> getOrderDetail(@Path("id") int orderId);

    // GET /api/pedidos/{id}/seguimiento
    @GET("pedidos/{id}/seguimiento")
    Call<ApiResponse<Order>> getOrderTracking(@Path("id") int orderId);

    // NUEVO: El agente confirma el pedido leyendo el QR (codigo_pedido)
    @POST("pedidos/confirmar_agente")
    Call<ApiResponse<Void>> confirmarPedidoAgente(@Body AgentConfirmRequest request);
}
