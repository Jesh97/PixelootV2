package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketTimelineBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemTimelineEventBinding;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class TicketTimelineFragment extends Fragment {

    private FragmentTicketTimelineBinding binding;

    public TicketTimelineFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketTimelineBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupTimeline();
    }

    private void setupTimeline() {
        List<String[]> events = new ArrayList<>();
        events.add(new String[]{"Ticket Creado", "Tu reporte ha sido recibido.", "24 Oct, 10:30 AM"});
        events.add(new String[]{"En Revisión", "Estamos validando la información.", "24 Oct, 02:45 PM"});

        binding.rvTimeline.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTimeline.setAdapter(new GenericAdapter<>(events, ItemTimelineEventBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_timeline_event.xml
            
            // 1. Título del evento (ej. Ticket Creado)
            itemBinding.txtTimelineTitle.setText(data[0]);
            
            // 2. Descripción detallada
            itemBinding.txtTimelineDesc.setText(data[1]);
            
            // 3. Marca de tiempo (Fecha/Hora)
            itemBinding.txtTimelineTime.setText(data[2]);
            
            // 4. Indicador visual de la línea de tiempo (Ajustar si es necesario)
            // =========================================================================
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
