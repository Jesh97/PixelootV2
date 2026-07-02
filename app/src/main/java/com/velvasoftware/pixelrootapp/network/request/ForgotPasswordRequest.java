package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordRequest {

    @SerializedName("correo")
    private String correo;

    public ForgotPasswordRequest(String correo) {
        this.correo = correo;
    }
}