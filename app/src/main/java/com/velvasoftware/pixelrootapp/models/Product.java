package com.velvasoftware.pixelrootapp.models;

public class Product {
    private int id;
    private String title;
    private String category;
    private double price;
    private String rating;
    private String imageUrl;
    private String description;

    public Product() {}

    public Product(int id, String title, String category, double price, String rating) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.rating = rating;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setRating(String rating) { this.rating = rating; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
}
