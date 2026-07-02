package com.velvasoftware.pixelrootapp.network.request;

import com.google.gson.annotations.SerializedName;

public class FcmTokenRequest {

    @SerializedName("token")
    private final String token;

    @SerializedName("dispositivo")
    private final String dispositivo;

    public FcmTokenRequest(String token, String dispositivo) {
        this.token       = token;
        this.dispositivo = dispositivo;
    }
}
