package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class SavedCard {

    @SerializedName("tarjeta_id")
    private int id;

    @SerializedName("titular")
    private String cardholderName;

    @SerializedName("ultimos_4")
    private String last4;

    @SerializedName("marca")
    private String brand;

    @SerializedName("expiracion")
    private String expiry;

    public int getId() { return id; }
    public String getCardholderName() { return cardholderName; }
    public String getLast4() { return last4; }
    public String getBrand() { return brand; }
    public String getExpiry() { return expiry; }
}