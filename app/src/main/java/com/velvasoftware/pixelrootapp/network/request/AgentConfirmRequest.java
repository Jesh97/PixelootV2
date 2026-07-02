package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class AgentConfirmRequest {

    @SerializedName("pedido_id")
    private final int pedidoId;

    public AgentConfirmRequest(int pedidoId) {
        this.pedidoId = pedidoId;
    }
}