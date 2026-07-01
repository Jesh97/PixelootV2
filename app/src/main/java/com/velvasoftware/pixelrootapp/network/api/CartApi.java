package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.network.request.AddToCartRequest;
import com.velvasoftware.pixelrootapp.network.request.UpdateCartRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface CartApi {

    // GET /api/carrito/ (requiere JWT)
    @GET("carrito/")
    Call<ApiResponse<CartResponse>> getCart();

    // POST /api/carrito/productos
    @POST("carrito/productos")
    Call<ApiResponse<CartResponse>> addProduct(@Body AddToCartRequest request);

    // PUT /api/carrito/productos/{juegoId}
    @PUT("carrito/productos/{juegoId}")
    Call<ApiResponse<CartResponse>> updateProduct(@Path("juegoId") int juegoId, @Body UpdateCartRequest request);

    // DELETE /api/carrito/productos/{juegoId}
    @DELETE("carrito/productos/{juegoId}")
    Call<ApiResponse<CartResponse>> removeProduct(@Path("juegoId") int juegoId);
}