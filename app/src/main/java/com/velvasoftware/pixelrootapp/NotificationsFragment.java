package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentNotificationsBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemNotificationBinding;
import com.velvasoftware.pixelrootapp.models.Notification;
import com.velvasoftware.pixelrootapp.network.api.NotificationApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private GenericAdapter<ItemNotificationBinding, Notification> adapter;
    private final List<Notification> notifications = new ArrayList<>();

    public NotificationsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        fetchNotifications();
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(notifications, ItemNotificationBinding::inflate, (itemBinding, data) -> {

            itemBinding.tvNotificationTitle.setText(data.getTitle());
            itemBinding.tvNotificationDescription.setText(data.getDescription());
            itemBinding.tvNotificationDateTime.setText(data.getDateTime());
            itemBinding.ivReadStatus.setVisibility(data.isRead() ? View.GONE : View.VISIBLE);

            View.OnClickListener abrirDetalle = v -> onNotificationClicked(data);
            itemBinding.tvViewDetail.setOnClickListener(abrirDetalle);
            itemBinding.getRoot().setOnClickListener(abrirDetalle);
        });

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void onNotificationClicked(Notification data) {
        // Si no está leída, la marcamos como leída en el backend (fire and forget)
        // y actualizamos el indicador visual localmente.
        if (!data.isRead()) {
            data.setRead(true);
            adapter.notifyDataSetChanged();
            RetrofitClient.getNotificationApi().markAsRead(data.getId()).enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                    // No se requiere acción adicional; el estado local ya se actualizó.
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                    // Silencioso: si falla, en la próxima carga de la lista se refleja el estado real.
                }
            });
        }

        if (data.getTicketId() != null) {
            Bundle args = new Bundle();
            args.putInt("ticketId", data.getTicketId());
            Navigation.findNavController(binding.getRoot()).navigate(R.id.ticketDetailFragment, args);
        } else {
            Toast.makeText(getContext(), "Esta notificación no tiene un ticket asociado", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchNotifications() {
        NotificationApi api = RetrofitClient.getNotificationApi();
        api.getNotifications().enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Notification>>> call,
                                   @NonNull Response<ApiResponse<List<Notification>>> response) {
                if (binding == null) return; // la vista pudo haberse destruido mientras esperábamos la respuesta

                ApiResponse<List<Notification>> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    notifications.clear();
                    notifications.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                    updateEmptyState(notifications.isEmpty() ? "No tienes notificaciones por ahora." : null);
                } else {
                    String message = body != null ? body.getMessage() : "No se pudieron cargar las notificaciones";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    updateEmptyState("No se pudieron cargar las notificaciones. Desliza para reintentar o vuelve a entrar a esta pantalla.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Notification>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                updateEmptyState("No se pudo conectar al servidor. Revisa tu conexión e inténtalo de nuevo.");
            }
        });
    }

    private void updateEmptyState(@Nullable String message) {
        if (binding == null) return;
        if (message == null) {
            binding.tvEmptyState.setVisibility(View.GONE);
        } else {
            binding.tvEmptyState.setText(message);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}