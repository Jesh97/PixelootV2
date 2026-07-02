package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Order {

    @SerializedName("pedido_id")
    private int orderId;

    @SerializedName("codigo_pedido")
    private String orderCode;

    @SerializedName("estado")
    private String status;

    @SerializedName("subtotal")
    private double subtotal;

    @SerializedName("impuesto")
    private double tax;

    @SerializedName("total")
    private double total;

    @SerializedName("metodo_pago")
    private String paymentMethod;

    @SerializedName("sucursal_id")
    private Integer branchId;

    @SerializedName("fecha_pedido")
    private String date;

    @SerializedName("codigo_pedido")
    private String orderCode;

    @SerializedName("items")
    private List<CartItem> items;

    public Order() {}

    public int getOrderId() { return orderId; }
    public String getOrderCode() { return orderCode; }
    public String getStatus() { return status; }
    public double getSubtotal() { return subtotal; }
    public double getTax() { return tax; }
    public double getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public Integer getBranchId() { return branchId; }
    public String getDate() { return date; }
    public String getOrderCode() { return orderCode; }
    public List<CartItem> getItems() { return items; }
}
