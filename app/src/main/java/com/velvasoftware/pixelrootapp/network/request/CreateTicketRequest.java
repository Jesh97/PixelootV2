package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class CreateTicketRequest {

    @SerializedName("pedido_id")
    private final int pedidoId;

    @SerializedName("tipo_ticket_id")
    private final int tipoTicketId;

    @SerializedName("prioridad_id")
    private final int prioridadId;

    @SerializedName("titulo")
    private final String titulo;

    @SerializedName("descripcion")
    private final String descripcion;

    @SerializedName("sucursal_id")
    private final Integer sucursalId;

    public CreateTicketRequest(int pedidoId, int tipoTicketId, int prioridadId, String titulo, String descripcion, Integer sucursalId) {
        this.pedidoId = pedidoId;
        this.tipoTicketId = tipoTicketId;
        this.prioridadId = prioridadId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.sucursalId = sucursalId;
    }
}