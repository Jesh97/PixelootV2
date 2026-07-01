package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Representa el objeto "data" que devuelve POST /api/auth/login:
 * { usuario_id, correo, nombre, apellido, rol_id, sucursal_id, token }
 */
public class User {

    @SerializedName("usuario_id")
    private int id;

    @SerializedName("correo")
    private String email;

    @SerializedName("nombre")
    private String firstName;

    @SerializedName("apellido")
    private String lastName;

    @SerializedName("rol_id")
    private int rolId;

    @SerializedName("sucursal_id")
    private Integer sucursalId;

    @SerializedName("token")
    private String token;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public int getRolId() { return rolId; }
    public void setRolId(int rolId) { this.rolId = rolId; }

    public Integer getSucursalId() { return sucursalId; }
    public void setSucursalId(Integer sucursalId) { this.sucursalId = sucursalId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}