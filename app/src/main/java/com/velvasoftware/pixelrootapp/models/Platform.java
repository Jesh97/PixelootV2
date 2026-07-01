package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa una plataforma tal como la devuelve GET /api/juegos/plataformas.
 * Campos reales del backend: plataforma_id, nombre.
 */
public class Platform {

    @SerializedName("plataforma_id")
    private int id;

    @SerializedName("nombre")
    private String name;

    public Platform() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}