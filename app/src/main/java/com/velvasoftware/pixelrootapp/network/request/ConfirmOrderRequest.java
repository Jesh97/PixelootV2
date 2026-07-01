package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class ConfirmOrderRequest {

    @SerializedName("sucursal_id")
    private final int sucursalId;

    @SerializedName("metodo_pago")
    private final String metodoPago;

    public ConfirmOrderRequest(int sucursalId, String metodoPago) {
        this.sucursalId = sucursalId;
        this.metodoPago = metodoPago;
    }
}