package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class TicketPriority {

    @SerializedName("prioridad_id")
    private int id;

    @SerializedName("nombre")
    private String name;

    public int getId() { return id; }
    public String getName() { return name; }
}