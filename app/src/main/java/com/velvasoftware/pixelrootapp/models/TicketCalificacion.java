package com.velvasoftware.pixelrootapp.models;

import com.google.gson.annotations.SerializedName;

/**
 * Corresponde a la tabla ticket_calificaciones:
 *
 * CREATE TABLE ticket_calificaciones (
 *     calificacion_id INT AUTO_INCREMENT PRIMARY KEY,
 *     ticket_id INT NOT NULL UNIQUE,
 *     usuario_id INT NOT NULL,
 *     calificacion TINYINT NOT NULL,
 *     comentario TEXT,
 *     creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *     ...
 * );
 */
public class TicketCalificacion {

    @SerializedName("calificacion_id")
    private int id;

    @SerializedName("ticket_id")
    private int ticketId;

    @SerializedName("usuario_id")
    private int userId;

    @SerializedName("calificacion")
    private int calificacion; // 1..5

    @SerializedName("comentario")
    private String comentario;

    @SerializedName("creado_en")
    private String creadoEn;

    public TicketCalificacion() {}

    public int getId() { return id; }
    public int getTicketId() { return ticketId; }
    public int getUserId() { return userId; }
    public int getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public String getCreadoEn() { return creadoEn; }
}