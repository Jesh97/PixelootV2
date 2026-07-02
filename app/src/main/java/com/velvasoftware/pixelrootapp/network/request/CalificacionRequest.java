package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class CalificacionRequest {

    @SerializedName("calificacion")
    private final int calificacion; // 1..5

    @SerializedName("comentario")
    private final String comentario;

    public CalificacionRequest(int calificacion, String comentario) {
        this.calificacion = calificacion;
        this.comentario = comentario;
    }
}