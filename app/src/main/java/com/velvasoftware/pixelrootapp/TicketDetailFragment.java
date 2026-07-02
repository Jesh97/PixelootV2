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
import com.velvasoftware.pixelrootapp.models.TicketCalificacion;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.TicketApi;
import com.velvasoftware.pixelrootapp.network.request.CalificacionRequest;
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
    private boolean ticketCerrado = false;

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
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

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

        binding.btnEnviarCalificacion.setOnClickListener(v -> onEnviarCalificacionClicked());
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

        ticketCerrado = ESTADOS_CERRADOS.contains(ticket.getStatusName());
        binding.btnReopenTicket.setVisibility(ticketCerrado ? View.VISIBLE : View.GONE);

        if (ticketCerrado) {
            binding.txtDetailStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blanco_intermedio)));
            binding.txtDetailStatus.setTextColor(getResources().getColor(R.color.negro_oscuro));
        } else {
            binding.txtDetailStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.verde_claro_pixel)));
            binding.txtDetailStatus.setTextColor(getResources().getColor(R.color.negro_oscuro));
        }

        // La calificación solo aplica una vez cerrado/resuelto/rechazado el caso.
        if (ticketCerrado) {
            binding.sectionCalificacion.setVisibility(View.VISIBLE);
            loadCalificacion();
        } else {
            binding.sectionCalificacion.setVisibility(View.GONE);
        }

        // Con el caso cerrado ya no tiene sentido seguir conversando, ver el timeline
        // en vivo ni subir más adjuntos, así que se deshabilitan esos accesos.
        setNavButtonsEnabled(!ticketCerrado);
    }

    private void setNavButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1f : 0.4f;

        binding.btnViewTimeline.setEnabled(enabled);
        binding.btnViewTimeline.setAlpha(alpha);

        binding.btnViewAttachments.setEnabled(enabled);
        binding.btnViewAttachments.setAlpha(alpha);

        binding.btnViewChat.setEnabled(enabled);
        binding.btnViewChat.setAlpha(alpha);
    }

    // =========================================================================
    // BACKEND: GET /api/tickets/{id}/calificacion
    // Revisa si el usuario ya calificó este ticket (ticket_id es UNIQUE en la tabla).
    // =========================================================================
    private void loadCalificacion() {
        TicketApi api = RetrofitClient.getTicketApi();
        api.getCalificacion(ticketId).enqueue(new Callback<ApiResponse<TicketCalificacion>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<TicketCalificacion>> call, @NonNull Response<ApiResponse<TicketCalificacion>> response) {
                if (binding == null) return;

                ApiResponse<TicketCalificacion> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    // Ya existe una calificación: se muestra en modo solo-lectura.
                    mostrarCalificacionExistente(body.getData());
                } else {
                    // Aún no calificado: se deja el formulario activo.
                    mostrarFormularioCalificacion();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<TicketCalificacion>> call, @NonNull Throwable t) {
                Log.e(TAG, "No se pudo verificar la calificación", t);
                if (binding == null) return;
                mostrarFormularioCalificacion();
            }
        });
    }

    private void mostrarCalificacionExistente(TicketCalificacion calificacion) {
        binding.ratingBarCalificacion.setRating(calificacion.getCalificacion());
        binding.ratingBarCalificacion.setIsIndicator(true);

        binding.etComentarioCalificacion.setText(calificacion.getComentario());
        binding.etComentarioCalificacion.setEnabled(false);

        binding.btnEnviarCalificacion.setVisibility(View.GONE);
        binding.txtCalificacionEnviada.setVisibility(View.VISIBLE);
        binding.txtCalificacionEnviada.setText("¡Gracias por calificar nuestra atención!");
    }

    private void mostrarFormularioCalificacion() {
        binding.ratingBarCalificacion.setIsIndicator(false);
        binding.etComentarioCalificacion.setEnabled(true);
        binding.btnEnviarCalificacion.setVisibility(View.VISIBLE);
        binding.txtCalificacionEnviada.setVisibility(View.GONE);
    }

    // =========================================================================
    // BACKEND: POST /api/tickets/{id}/calificacion
    // =========================================================================
    private void onEnviarCalificacionClicked() {
        int estrellas = (int) binding.ratingBarCalificacion.getRating();
        if (estrellas < 1) {
            Toast.makeText(getContext(), "Selecciona al menos 1 estrella", Toast.LENGTH_SHORT).show();
            return;
        }

        String comentario = binding.etComentarioCalificacion.getText() != null
                ? binding.etComentarioCalificacion.getText().toString().trim()
                : "";

        binding.btnEnviarCalificacion.setEnabled(false);

        TicketApi api = RetrofitClient.getTicketApi();
        api.enviarCalificacion(ticketId, new CalificacionRequest(estrellas, comentario))
                .enqueue(new Callback<ApiResponse<TicketCalificacion>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<TicketCalificacion>> call, @NonNull Response<ApiResponse<TicketCalificacion>> response) {
                        if (binding == null) return;
                        binding.btnEnviarCalificacion.setEnabled(true);

                        ApiResponse<TicketCalificacion> body = response.body();
                        if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                            mostrarCalificacionExistente(body.getData());
                            Toast.makeText(getContext(), "¡Gracias por tu calificación!", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = body != null ? body.getMessage() : "No se pudo enviar la calificación";
                            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<TicketCalificacion>> call, @NonNull Throwable t) {
                        if (binding == null) return;
                        binding.btnEnviarCalificacion.setEnabled(true);
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}