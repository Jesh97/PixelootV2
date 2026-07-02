package com.velvasoftware.pixelrootapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketDetailBinding;
import com.velvasoftware.pixelrootapp.models.Ticket;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.TicketApi;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketDetailFragment extends Fragment {

    private static final String TAG = "TicketDetailFragment";
    private static final List<String> ESTADOS_CERRADOS = Arrays.asList("RESUELTO", "CERRADO", "RECHAZADO");

    private FragmentTicketDetailBinding binding;
    private int ticketId = -1;

    public TicketDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ticketId = getArguments() != null ? getArguments().getInt("ticketId", -1) : -1;

        if (ticketId <= 0) {
            Toast.makeText(getContext(), "No se encontró el ticket", Toast.LENGTH_SHORT).show();
            return;
        }

        setupListeners();
        loadTicketDetail();
    }

    private void setupListeners() {
        binding.btnViewChat.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("ticketId", ticketId);
            Navigation.findNavController(v).navigate(R.id.ticketChatFragment, args);
        });

        binding.btnViewTimeline.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("ticketId", ticketId);
            Navigation.findNavController(v).navigate(R.id.ticketTimelineFragment, args);
        });

        binding.btnViewAttachments.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("ticketId", ticketId);
            Navigation.findNavController(v).navigate(R.id.ticketAttachmentsFragment, args);
        });

        binding.btnReopenTicket.setOnClickListener(v -> {
            // TODO: Lógica para reabrir ticket
        });
    }

    // =========================================================================
    // BACKEND: GET /api/tickets/{id}
    // =========================================================================
    private void loadTicketDetail() {
        TicketApi api = RetrofitClient.getTicketApi();
        api.getTicketDetail(ticketId).enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Ticket>> call, @NonNull Response<ApiResponse<Ticket>> response) {
                if (binding == null) return;

                ApiResponse<Ticket> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindTicket(body.getData());
                } else {
                    Log.e(TAG, "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudo cargar el ticket", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Ticket>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e(TAG, "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindTicket(Ticket ticket) {
        binding.txtDetailId.setText("Ticket #" + ticket.getId());
        binding.txtDetailTitle.setText(ticket.getTitle());
        binding.txtDetailDescription.setText(ticket.getDescription());
        binding.txtDetailOrder.setText("#" + ticket.getOrderId());
        binding.txtDetailStatus.setText(ticket.getStatusName());

        boolean cerrado = ESTADOS_CERRADOS.contains(ticket.getStatusName());
        binding.btnReopenTicket.setVisibility(cerrado ? View.VISIBLE : View.GONE);

        if (cerrado) {
            binding.txtDetailStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blanco_intermedio)));
            binding.txtDetailStatus.setTextColor(getResources().getColor(R.color.negro_oscuro));
        } else {
            binding.txtDetailStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.verde_claro_pixel)));
            binding.txtDetailStatus.setTextColor(getResources().getColor(R.color.negro_oscuro));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}