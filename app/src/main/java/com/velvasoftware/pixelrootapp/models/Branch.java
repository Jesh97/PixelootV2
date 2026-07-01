package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

public class Branch {

    @SerializedName("sucursal_id")
    private int id;

    @SerializedName("nombre")
    private String name;

    @SerializedName("direccion")
    private String address;

    @SerializedName("ciudad")
    private String city;

    @SerializedName("telefono")
    private String phone;

    @SerializedName("latitud")
    private double latitude;

    @SerializedName("longitud")
    private double longitude;

    public Branch() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}