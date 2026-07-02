package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Ticket;
import com.velvasoftware.pixelrootapp.models.TicketPriority;
import com.velvasoftware.pixelrootapp.models.TicketType;
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
}