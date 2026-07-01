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

import com.velvasoftware.pixelrootapp.databinding.FragmentNotificationsBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemNotificationBinding;
import com.velvasoftware.pixelrootapp.models.Notification;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private GenericAdapter<ItemNotificationBinding, Notification> adapter;
    private List<Notification> notifications = new ArrayList<>();

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
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_notification.xml
            
            // 1. Título de la notificación
            itemBinding.tvNotificationTitle.setText(data.getTitle());
            
            // 2. Descripción o cuerpo
            itemBinding.tvNotificationDescription.setText(data.getDescription());
            
            // 3. Fecha y hora
            itemBinding.tvNotificationDateTime.setText(data.getDateTime());
            
            // 4. Estado de lectura (indicador visual)
            itemBinding.ivReadStatus.setVisibility(data.isRead() ? View.GONE : View.VISIBLE);
            
            // 5. Acción al pulsar "Ver detalle" o el item
            itemBinding.tvViewDetail.setOnClickListener(v -> {
                // Implementar navegación según el tipo de notificación
                Toast.makeText(getContext(), "Navegando al detalle...", Toast.LENGTH_SHORT).show();
            });
            // =========================================================================
        });

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvNotifications.setAdapter(adapter);
    }

    private void fetchNotifications() {
        // TODO: Implementar llamada a API con Retrofit
        // Bloque de ejemplo para el backend
        loadMockData();
    }

    private void loadMockData() {
        notifications.clear();
        notifications.add(new Notification(1, "Actualización de Ticket #123", "Tu ticket ha sido respondido.", "24/05/2024 10:00 AM", false));
        notifications.add(new Notification(2, "Nuevo Ticket Creado", "Se ha registrado exitosamente.", "23/05/2024 03:20 PM", true));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
