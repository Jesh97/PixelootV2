package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {
    @SerializedName("mensaje_id")
    private int id;

    @SerializedName("ticket_id")
    private int ticketId;

    @SerializedName("usuario_id")
    private int usuarioId;

    @SerializedName("es_agente")
    private int esAgente; // 0 o 1

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("leido")
    private int leido;

    @SerializedName("creado_en")
    private String creadoEn;

    public ChatMessage() {}

    public int getId() { return id; }
    public int getTicketId() { return ticketId; }
    public int getUsuarioId() { return usuarioId; }
    public boolean isFromAgent() { return esAgente == 1; }
    public String getMessage() { return mensaje; }
    public boolean isLeido() { return leido == 1; }
    public String getCreadoEn() { return creadoEn; }

    /** true = mensaje del usuario logueado (se alinea a la derecha) */
    public boolean isFromUser() {
        return !isFromAgent();
    }
}