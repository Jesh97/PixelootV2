package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class SaveCardRequest {

    @SerializedName("titular")
    private final String titular;

    @SerializedName("numero_tarjeta")
    private final String numeroTarjeta;

    @SerializedName("expiracion")
    private final String expiracion;

    @SerializedName("marca")
    private final String marca;

    public SaveCardRequest(String titular, String numeroTarjeta, String expiracion, String marca) {
        this.titular = titular;
        this.numeroTarjeta = numeroTarjeta;
        this.expiracion = expiracion;
        this.marca = marca;
    }
}