package com.velvasoftware.pixelrootapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.velvasoftware.pixelrootapp.models.TicketPriority;
import com.velvasoftware.pixelrootapp.models.TicketType;

import java.util.List;

public class TicketOptionsResponse {

    @SerializedName("tipos")
    private List<TicketType> tipos;

    @SerializedName("prioridades")
    private List<TicketPriority> prioridades;

    public List<TicketType> getTipos() { return tipos; }
    public List<TicketPriority> getPrioridades() { return prioridades; }
}