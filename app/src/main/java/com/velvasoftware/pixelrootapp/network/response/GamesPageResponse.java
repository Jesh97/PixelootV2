package com.velvasoftware.pixelrootapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.velvasoftware.pixelrootapp.models.Product;

import java.util.List;

/**
 * "data" de GET /api/juegos/ (viene paginado, a diferencia de /destacados,
 * /populares y /recomendados que devuelven una lista simple).
 */
public class GamesPageResponse {

    @SerializedName("juegos")
    private List<Product> juegos;

    @SerializedName("total")
    private int total;

    @SerializedName("pagina")
    private int pagina;

    @SerializedName("total_paginas")
    private int totalPaginas;

    public List<Product> getJuegos() { return juegos; }
    public int getTotal() { return total; }
    public int getPagina() { return pagina; }
    public int getTotalPaginas() { return totalPaginas; }
}