package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BranchApi {

    // GET /api/sucursales/
    @GET("sucursales/")
    Call<ApiResponse<List<Branch>>> getBranches();

    // GET /api/sucursales/{id}
    @GET("sucursales/{id}")
    Call<ApiResponse<Branch>> getBranchById(@Path("id") int branchId);

    // GET /api/sucursales/buscar?q=texto
    @GET("sucursales/buscar")
    Call<ApiResponse<List<Branch>>> buscarSucursales(@Query("q") String query);


}