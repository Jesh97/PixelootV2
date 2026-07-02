package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {

    @SerializedName("correo")
    private String correo;

    @SerializedName("codigo")
    private String codigo;

    @SerializedName("contrasena")
    private String contrasena;

    public ResetPasswordRequest(String correo, String codigo, String contrasena) {
        this.correo = correo;
        this.codigo = codigo;
        this.contrasena = contrasena;
    }
}