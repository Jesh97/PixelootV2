package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa un "juego" tal como lo devuelve VELVA-API (GET /api/juegos, /api/juegos/{id},
 * /api/juegos/destacados, /api/juegos/populares, /api/juegos/recomendados).
 *
 * Campos reales del backend: juego_id, titulo, descripcion, precio, stock_global,
 * imagen_portada, imagen_banner, es_digital, calificacion_promedio, fecha_lanzamiento,
 * categoria_id, plataforma_id.
 *
 * "category" y "rating" se mantienen como campos de UI de solo lectura (no vienen tal
 * cual del backend) para no romper las pantallas que todavía usan datos mock; se
 * calculan/asignan a partir de categoriaId / calificacionPromedio cuando haga falta.
 */
public class Product {

    @SerializedName("juego_id")
    private int id;

    @SerializedName("titulo")
    private String title;

    @SerializedName("descripcion")
    private String description;

    @SerializedName("precio")
    private double price;

    @SerializedName("stock_global")
    private int stock;

    @SerializedName("imagen_portada")
    private String imageUrl;

    @SerializedName("imagen_banner")
    private String bannerUrl;

    @SerializedName("es_digital")
    private int digital;

    @SerializedName("calificacion_promedio")
    private double ratingValue;

    @SerializedName("fecha_lanzamiento")
    private String releaseDate;

    @SerializedName("categoria_id")
    private int categoriaId;

    @SerializedName("plataforma_id")
    private int plataformaId;

    // No vienen del JSON del backend; se rellenan manualmente en el Fragment si se necesitan.
    private transient String category;
    private transient String rating;

    public Product() {}

    /** Constructor de compatibilidad usado por pantallas que aún no están conectadas a la API. */
    public Product(int id, String title, String category, double price, String rating) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.rating = rating;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getBannerUrl() { return bannerUrl; }
    public void setBannerUrl(String bannerUrl) { this.bannerUrl = bannerUrl; }

    public int getDigital() { return digital; }
    public void setDigital(int digital) { this.digital = digital; }
    public boolean isDigital() { return digital == 1; }

    public double getRatingValue() { return ratingValue; }
    public void setRatingValue(double ratingValue) { this.ratingValue = ratingValue; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public int getCategoriaId() { return categoriaId; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }

    public int getPlataformaId() { return plataformaId; }
    public void setPlataformaId(int plataformaId) { this.plataformaId = plataformaId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    /** Devuelve el rating manual si se seteó, si no lo formatea a partir de calificacion_promedio. */
    public String getRating() {
        if (rating != null) return rating;
        return String.format("★ %.1f", ratingValue);
    }
    public void setRating(String rating) { this.rating = rating; }
}