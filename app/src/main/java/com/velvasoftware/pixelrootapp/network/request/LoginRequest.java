package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * Body de POST /api/auth/login.
 * El backend Flask espera exactamente estos nombres de campo.
 */
public class LoginRequest {

    @SerializedName("correo")
    private String correo;

    @SerializedName("contrasena")
    private String contrasena;

    public LoginRequest(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}