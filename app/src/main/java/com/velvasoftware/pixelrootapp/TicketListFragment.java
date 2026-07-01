package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketListBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemTicketCardBinding;
import com.velvasoftware.pixelrootapp.models.Ticket;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class TicketListFragment extends Fragment {

    private FragmentTicketListBinding binding;
    private GenericAdapter<ItemTicketCardBinding, Ticket> adapter;
    private List<Ticket> ticketList = new ArrayList<>();

    public TicketListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        fetchTickets();
        
        binding.btnNewTicket.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.createTicketFragment)
        );
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(ticketList, ItemTicketCardBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_ticket_card.xml
            
            // 1. ID del Ticket
            itemBinding.txtTicketId.setText("Ticket #" + data.getId());
            
            // 2. Asunto / Problema
            itemBinding.txtTicketSubject.setText(data.getSubject());
            
            // 3. Estado (ABIERTO, RESUELTO, EN PROCESO)
            itemBinding.txtTicketStatus.setText(data.getStatus());
            
            // 4. Pedido Relacionado
            itemBinding.txtRelatedOrder.setText("Pedido #" + data.getRelatedOrderId());
            
            // Estilización según estado
            if ("RESUELTO".equals(data.getStatus())) {
                itemBinding.txtTicketStatus.setAlpha(0.6f);
                itemBinding.txtTicketStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.blanco_intermedio)));
            } else {
                itemBinding.txtTicketStatus.setAlpha(1.0f);
                itemBinding.txtTicketStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.verde_claro_pixel)));
            }

            // 5. Navegación al chat del ticket
            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("ticketId", data.getId());
                Navigation.findNavController(v).navigate(R.id.ticketDetailFragment, args);
            });
            // =========================================================================
        });

        binding.rvTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTickets.setAdapter(adapter);
    }

    private void fetchTickets() {
        // TODO: Implementar llamada a API de Soporte
        loadMockData();
    }

    private void loadMockData() {
        ticketList.clear();
        ticketList.add(new Ticket("9283", "Error en descarga de código", "ABIERTO", "PR-85920"));
        ticketList.add(new Ticket("8144", "Modificación de datos", "RESUELTO", "PR-74211"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
