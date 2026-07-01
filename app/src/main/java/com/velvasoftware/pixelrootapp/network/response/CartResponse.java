package com.velvasoftware.pixelrootapp.network.response;

import com.google.gson.annotations.SerializedName;
import com.velvasoftware.pixelrootapp.models.CartItem;

import java.util.List;

public class CartResponse {

    @SerializedName("pedido_id")
    private Integer pedidoId;

    @SerializedName("items")
    private List<CartItem> items;

    @SerializedName("subtotal")
    private double subtotal;

    public Integer getPedidoId() { return pedidoId; }
    public List<CartItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
}