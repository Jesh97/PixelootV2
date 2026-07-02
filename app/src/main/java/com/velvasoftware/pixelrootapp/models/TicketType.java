package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class TicketType {

    @SerializedName("tipo_ticket_id")
    private int id;

    @SerializedName("nombre")
    private String name;

    @SerializedName("descripcion")
    private String description;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}