package com.velvasoftware.pixelrootapp.models;

public class Branch {
    private int id;
    private String name;
    private String address;
    private String distance;
    private double latitude;
    private double longitude;

    public Branch() {}

    public Branch(int id, String name, String address, String distance) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.distance = distance;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
