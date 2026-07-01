package com.velvasoftware.pixelrootapp.models;

import java.util.List;

public class Order {
    private String orderId;
    private String date;
    private String status;
    private double total;
    private List<Product> products;
    private String qrCode;

    public Order() {}

    public Order(String orderId, String date, String status, double total) {
        this.orderId = orderId;
        this.date = date;
        this.status = status;
        this.total = total;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
