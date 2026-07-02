package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class Ticket {

    @SerializedName("ticket_id")
    private int id;

    @SerializedName("titulo")
    private String title;

    @SerializedName("descripcion")
    private String description;

    @SerializedName("pedido_id")
    private int orderId;

    @SerializedName("tipo_ticket_id")
    private int typeId;

    // GET /api/tickets/ (cliente) devuelve "tipo_ticket"; otros endpoints de
    // agente/admin devuelven "tipo_ticket_nombre". Con "alternate" el mismo
    // modelo sirve para ambos sin importar cuál los llene.
    @SerializedName(value = "tipo_ticket_nombre", alternate = {"tipo_ticket"})
    private String typeName;

    @SerializedName("prioridad_id")
    private int priorityId;

    @SerializedName(value = "prioridad_nombre", alternate = {"prioridad"})
    private String priorityName;

    @SerializedName("estado_id")
    private int statusId;

    @SerializedName(value = "estado_nombre", alternate = {"estado"})
    private String statusName;

    @SerializedName("creado_en")
    private String createdAt;

    @SerializedName("actualizado_en")
    private String updatedAt;

    @SerializedName("cerrado_en")
    private String closedAt;

    public Ticket() {}

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getOrderId() { return orderId; }
    public int getTypeId() { return typeId; }
    public String getTypeName() { return typeName; }
    public int getPriorityId() { return priorityId; }
    public String getPriorityName() { return priorityName; }
    public int getStatusId() { return statusId; }
    public String getStatusName() { return statusName; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getClosedAt() { return closedAt; }
}