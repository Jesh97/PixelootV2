package com.velvasoftware.pixelrootapp.network.api;

import com.velvasoftware.pixelrootapp.models.Ticket;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TicketApi {
    @GET("tickets")
    Call<List<Ticket>> getTickets();

    @POST("tickets")
    Call<Ticket> createTicket(@Body Ticket ticket);

    @GET("tickets/{id}")
    Call<Ticket> getTicketDetail(@Path("id") String ticketId);
}
