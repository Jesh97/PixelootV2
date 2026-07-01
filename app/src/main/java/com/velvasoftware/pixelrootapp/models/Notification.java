package com.velvasoftware.pixelrootapp.models;

public class Notification {
    private int id;
    private String title;
    private String description;
    private String dateTime;
    private boolean isRead;

    public Notification() {}

    public Notification(int id, String title, String description, String dateTime, boolean isRead) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
        this.isRead = isRead;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
}
