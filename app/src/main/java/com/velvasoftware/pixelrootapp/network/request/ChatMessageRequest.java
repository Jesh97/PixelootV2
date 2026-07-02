package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class ChatMessageRequest {
    @SerializedName("mensaje")
    private String mensaje;

    public ChatMessageRequest(String mensaje) {
        this.mensaje = mensaje;
    }
}