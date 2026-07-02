package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class ConfirmOrderRequest {

    @SerializedName("sucursal_id")
    private final Integer sucursalId;

    @SerializedName("metodo_pago")
    private final String metodoPago;

    @SerializedName("codigo_pedido")
    private final String codigoPedido;

    public ConfirmOrderRequest(Integer sucursalId, String metodoPago, String codigoPedido) {
        this.sucursalId = sucursalId;
        this.metodoPago = metodoPago;
        this.codigoPedido = codigoPedido;
    }
}