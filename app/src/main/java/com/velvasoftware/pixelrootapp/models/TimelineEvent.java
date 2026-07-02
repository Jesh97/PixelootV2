package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class TimelineEvent {
    @SerializedName("historial_id")
    private int id;

    @SerializedName("accion")
    private String accion;

    @SerializedName("valor_anterior")
    private String valorAnterior;

    @SerializedName("valor_nuevo")
    private String valorNuevo;

    @SerializedName("comentario")
    private String comentario;

    @SerializedName("creado_en")
    private String creadoEn;

    public TimelineEvent() {}

    /** Constructor para el evento sintético "Ticket Creado" (no viene de ticket_historial). */
    public TimelineEvent(String valorNuevo, String comentario, String creadoEn) {
        this.valorNuevo = valorNuevo;
        this.comentario = comentario;
        this.creadoEn = creadoEn;
    }

    public String getValorNuevo() { return valorNuevo; }
    public String getComentario() { return comentario; }
    public String getCreadoEn() { return creadoEn; }
}