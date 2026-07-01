package com.velvasoftware.pixelrootapp.models;

public class Ticket {
    private String id;
    private String subject;
    private String status;
    private String relatedOrderId;
    private String createdAt;

    public Ticket() {}

    public Ticket(String id, String subject, String status, String relatedOrderId) {
        this.id = id;
        this.subject = subject;
        this.status = status;
        this.relatedOrderId = relatedOrderId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRelatedOrderId() { return relatedOrderId; }
    public void setRelatedOrderId(String relatedOrderId) { this.relatedOrderId = relatedOrderId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
