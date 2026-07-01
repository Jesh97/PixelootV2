package com.velvasoftware.pixelrootapp.network.response;

/**
 * Envoltorio genérico para las respuestas de VELVA-API.
 * Todos los endpoints Flask devuelven este mismo formato:
 *   { "status": true/false, "data": ..., "message": "..." }
 *
 * Ejemplo de uso con Retrofit:
 *   Call<ApiResponse<List<Product>>> call = api.getProducts();
 */
public class ApiResponse<T> {
    private boolean status;
    private T data;
    private String message;

    public boolean isStatus() { return status; }
    public T getData() { return data; }
    public String getMessage() { return message; }
}