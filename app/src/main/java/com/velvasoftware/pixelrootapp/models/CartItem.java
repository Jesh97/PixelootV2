package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class CartItem {

    @SerializedName("detalle_id")
    private int detalleId;

    @SerializedName("juego_id")
    private int juegoId;

    @SerializedName("titulo")
    private String title;

    @SerializedName("imagen_portada")
    private String imageUrl;

    @SerializedName("precio_unitario")
    private double unitPrice;

    @SerializedName("cantidad")
    private int quantity;

    @SerializedName("subtotal")
    private double subtotal;

    public int getDetalleId() { return detalleId; }
    public int getJuegoId() { return juegoId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }
}