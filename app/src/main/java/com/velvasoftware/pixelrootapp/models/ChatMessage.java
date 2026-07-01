package com.velvasoftware.pixelrootapp.models;

public class ChatMessage {
    private String senderId;
    private String message;
    private long timestamp;
    private boolean isFromUser;

    public ChatMessage() {} // Requerido para Firebase

    public ChatMessage(String senderId, String message, long timestamp, boolean isFromUser) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.isFromUser = isFromUser;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isFromUser() { return isFromUser; }
}
