package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.ChatMessage;
import com.velvasoftware.pixelrootapp.models.Ticket;
import com.velvasoftware.pixelrootapp.models.TicketImage;
import com.velvasoftware.pixelrootapp.models.TicketPriority;
import com.velvasoftware.pixelrootapp.models.TicketType;
import com.velvasoftware.pixelrootapp.models.TimelineEvent;
import com.velvasoftware.pixelrootapp.network.request.AttachmentUrlRequest;
import com.velvasoftware.pixelrootapp.network.request.ChatMessageRequest;
import com.velvasoftware.pixelrootapp.network.request.CreateTicketRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TicketApi {

    // GET /api/tickets/tipos (pública, no requiere JWT)
    @GET("tickets/tipos")
    Call<ApiResponse<List<TicketType>>> getTicketTypes();

    // GET /api/tickets/prioridades (pública, no requiere JWT)
    @GET("tickets/prioridades")
    Call<ApiResponse<List<TicketPriority>>> getTicketPriorities();

    // GET /api/tickets/
    @GET("tickets/")
    Call<ApiResponse<List<Ticket>>> getTickets();

    // POST /api/tickets/
    @POST("tickets/")
    Call<ApiResponse<Ticket>> createTicket(@Body CreateTicketRequest request);

    // GET /api/tickets/{id}
    @GET("tickets/{id}")
    Call<ApiResponse<Ticket>> getTicketDetail(@Path("id") int ticketId);

    // POST /api/tickets/{id}/reabrir
    @POST("tickets/{id}/reabrir")
    Call<ApiResponse<Ticket>> reopenTicket(@Path("id") int ticketId);

    // GET /api/tickets/{id}/mensajes
    @GET("tickets/{id}/mensajes")
    Call<ApiResponse<List<ChatMessage>>> getMensajes(@Path("id") int ticketId);

    // POST /api/tickets/{id}/mensajes
    @POST("tickets/{id}/mensajes")
    Call<ApiResponse<Object>> enviarMensaje(@Path("id") int ticketId, @Body ChatMessageRequest body);

    // GET /api/tickets/{id}/historial
    @GET("tickets/{id}/historial")
    Call<ApiResponse<List<TimelineEvent>>> getHistorial(@Path("id") int ticketId);

    @GET("tickets/{id}/imagenes")
    Call<ApiResponse<List<TicketImage>>> getImagenes(@Path("id") int ticketId);

    @POST("tickets/{id}/imagenes")
    Call<ApiResponse<Object>> subirUrlImagen(@Path("id") int ticketId, @Body AttachmentUrlRequest body);
}