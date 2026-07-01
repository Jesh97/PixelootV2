package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Category;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.GamesPageResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CatalogApi {

    // GET /api/categorias/
    @GET("categorias/")
    Call<ApiResponse<List<Category>>> getCategories();

    // GET /api/categorias/{id}
    @GET("categorias/{id}")
    Call<ApiResponse<Category>> getCategoryById(@Path("id") int categoryId);

    // GET /api/juegos/  -> paginado: data = { juegos, total, pagina, total_paginas }
    @GET("juegos/")
    Call<ApiResponse<GamesPageResponse>> getProducts(@Query("pagina") int pagina);

    // GET /api/juegos/{id}
    @GET("juegos/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") int productId);

    // GET /api/juegos/destacados -> data = lista simple de juegos
    @GET("juegos/destacados")
    Call<ApiResponse<List<Product>>> getFeaturedProducts();

    // GET /api/juegos/populares -> data = lista simple de juegos
    @GET("juegos/populares")
    Call<ApiResponse<List<Product>>> getPopularProducts();

    // GET /api/juegos/recomendados -> data = lista simple de juegos
    @GET("juegos/recomendados")
    Call<ApiResponse<List<Product>>> getRecommendedProducts();
}