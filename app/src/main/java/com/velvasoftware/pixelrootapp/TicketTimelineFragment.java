package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketTimelineBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemTimelineEventBinding;
import com.velvasoftware.pixelrootapp.models.TimelineEvent;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketTimelineFragment extends Fragment {

    private static final String TAG = "TicketTimelineFragment";

    private FragmentTicketTimelineBinding binding;
    private final List<TimelineEvent> events = new ArrayList<>();
    private int ticketId = -1;

    public TicketTimelineFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketTimelineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ticketId = getArguments() != null ? getArguments().getInt("ticketId", -1) : -1;

        setupTimeline();

        if (ticketId > 0) {
            loadHistorial();
        } else {
            Toast.makeText(getContext(), "No se encontró el ticket", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTimeline() {
        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTimeline.setAdapter(new GenericAdapter<>(events, ItemTimelineEventBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtTimelineTitle.setText(formatEstado(data.getValorNuevo()));
            itemBinding.txtTimelineDesc.setText(data.getComentario() != null ? data.getComentario() : "");
            itemBinding.txtTimelineTime.setText(data.getCreadoEn() != null ? data.getCreadoEn() : "");
        }));
    }

    private void loadHistorial() {
        RetrofitClient.getTicketApi().getHistorial(ticketId).enqueue(new Callback<ApiResponse<List<TimelineEvent>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TimelineEvent>>> call, @NonNull Response<ApiResponse<List<TimelineEvent>>> response) {
                if (binding == null) return;

                ApiResponse<List<TimelineEvent>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    events.clear();
                    events.add(new TimelineEvent("Ticket Creado", "Tu reporte ha sido recibido.", null));
                    events.addAll(body.getData());
                    binding.rvTimeline.getAdapter().notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error al cargar historial: " + response.code());
                    Toast.makeText(getContext(), "No se pudo cargar la línea de tiempo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TimelineEvent>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión historial", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Convierte "EN_PROGRESO" -> "En Progreso" para mostrar más legible. */
    private String formatEstado(String estado) {
        if (estado == null) return "";
        if ("Ticket Creado".equals(estado)) return estado;

        String[] palabras = estado.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String palabra : palabras) {
            if (palabra.isEmpty()) continue;
            sb.append(Character.toUpperCase(palabra.charAt(0))).append(palabra.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}