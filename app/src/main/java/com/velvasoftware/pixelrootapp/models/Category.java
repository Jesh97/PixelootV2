package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa una categoría tal como la devuelve GET /api/categorias.
 * Campos reales del backend: categoria_id, nombre, descripcion.
 */
public class Category {

    @SerializedName("categoria_id")
    private int id;

    @SerializedName("nombre")
    private String name;

    @SerializedName("descripcion")
    private String description;

    // No viene del backend todavía; queda listo para cuando se agregue en la BD/API.
    private transient String iconUrl;

    public Category() {}

    public Category(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}