package com.velvasoftware.pixelrootapp.network.response;

import com.velvasoftware.pixelrootapp.models.User;

public class LoginResponse {
    private boolean success;
    private String message;
    private User data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public User getData() { return data; }
    public void setData(User data) { this.data = data; }
}
