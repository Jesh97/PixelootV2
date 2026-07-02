package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class VerifyResetCodeRequest {

    @SerializedName("correo")
    private String correo;

    @SerializedName("codigo")
    private String codigo;

    public VerifyResetCodeRequest(String correo, String codigo) {
        this.correo = correo;
        this.codigo = codigo;
    }
}