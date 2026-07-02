package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class AttachmentUrlRequest {
    @SerializedName("url_imagen_ticket")
    private String urlImagenTicket;

    public AttachmentUrlRequest(String url) {
        this.urlImagenTicket = url;
    }
}