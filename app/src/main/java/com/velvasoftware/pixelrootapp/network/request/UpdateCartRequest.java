package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class UpdateCartRequest {

    @SerializedName("cantidad")
    private final int cantidad;

    public UpdateCartRequest(int cantidad) {
        this.cantidad = cantidad;
    }
}