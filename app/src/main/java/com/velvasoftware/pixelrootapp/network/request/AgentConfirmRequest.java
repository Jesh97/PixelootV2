package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class AgentConfirmRequest {

    @SerializedName("codigo_pedido")
    private final String codigoPedido;

    public AgentConfirmRequest(String codigoPedido) {
        this.codigoPedido = codigoPedido;
    }

    public String getCodigoPedido() {
        return codigoPedido;
    }
}
