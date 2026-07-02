package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class TicketImage {
    @SerializedName("imagen_id")
    private int id;

    @SerializedName("url_imagen_ticket")
    private String url;

    @SerializedName("creado_en")
    private String creadoEn;

    public String getUrl() { return url; }
}