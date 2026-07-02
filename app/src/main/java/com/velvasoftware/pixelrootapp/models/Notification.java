package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa un elemento de la lista devuelta por GET /api/notificaciones/.
 * Los nombres de campo coinciden con lo que envía el backend Flask.
 */
public class Notification {

    @SerializedName("notificacion_id")
    private int id;

    @SerializedName("ticket_id")
    private Integer ticketId;

    @SerializedName("titulo")
    private String title;

    @SerializedName("mensaje")
    private String description;

    @SerializedName("tipo")
    private String type;

    @SerializedName("creado_en")
    private String dateTime;

    @SerializedName("leido")
    private int leidoRaw;

    public Notification() {}

    public Notification(int id, String title, String description, String dateTime, boolean isRead) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.leidoRaw = isRead ? 1 : 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getTicketId() { return ticketId; }
    public void setTicketId(Integer ticketId) { this.ticketId = ticketId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public boolean isRead() { return leidoRaw != 0; }
    public void setRead(boolean read) { leidoRaw = read ? 1 : 0; }
}