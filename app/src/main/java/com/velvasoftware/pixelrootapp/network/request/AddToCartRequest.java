package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {

    @SerializedName("juego_id")
    private final int juegoId;

    @SerializedName("cantidad")
    private final int cantidad;

    public AddToCartRequest(int juegoId, int cantidad) {
        this.juegoId = juegoId;
        this.cantidad = cantidad;
    }
}