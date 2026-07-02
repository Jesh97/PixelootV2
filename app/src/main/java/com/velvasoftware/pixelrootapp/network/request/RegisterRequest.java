package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * Body de POST /api/auth/register.
 * Registra siempre un usuario con rol CLIENTE (rol_id = 1); el backend
 * ignora cualquier otro rol para este endpoint público.
 */
public class RegisterRequest {

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("dni")
    private String dni;

    @SerializedName("correo")
    private String correo;

    @SerializedName("contrasena")
    private String contrasena;

    public RegisterRequest(String nombre, String apellido, String dni, String correo, String contrasena) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.correo = correo;
        this.contrasena = contrasena;
    }
}