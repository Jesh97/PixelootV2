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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketListBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemTicketCardBinding;
import com.velvasoftware.pixelrootapp.models.Ticket;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.TicketApi;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketListFragment extends Fragment {

    private FragmentTicketListBinding binding;
    private GenericAdapter<ItemTicketCardBinding, Ticket> adapter;
    private final List<Ticket> ticketList = new ArrayList<>();

    private static final List<String> ESTADOS_CERRADOS = Arrays.asList("RESUELTO", "CERRADO", "RECHAZADO");

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

    @Override
    public void onResume() {
        super.onResume();
        // Por si el usuario acaba de crear un ticket y vuelve a esta pantalla.
        fetchTickets();
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(ticketList, ItemTicketCardBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtTicketId.setText("Ticket #" + data.getId());
            itemBinding.txtTicketSubject.setText(data.getTitle());
            itemBinding.txtTicketStatus.setText(data.getStatusName());
            itemBinding.txtRelatedOrder.setText("Pedido #" + data.getOrderId());

            boolean cerrado = ESTADOS_CERRADOS.contains(data.getStatusName());
            if (cerrado) {
                itemBinding.txtTicketStatus.setAlpha(0.6f);
                itemBinding.txtTicketStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blanco_intermedio)));
                itemBinding.txtTicketStatus.setTextColor(getResources().getColor(R.color.negro_oscuro));
            } else {
                itemBinding.txtTicketStatus.setAlpha(1.0f);
                itemBinding.txtTicketStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.verde_claro_pixel)));
                itemBinding.txtTicketStatus.setTextColor(getResources().getColor(R.color.negro_oscuro));
            }

            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("ticketId", data.getId());
                Navigation.findNavController(v).navigate(R.id.ticketDetailFragment, args);
            });
        });

        binding.rvTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvTickets.setAdapter(adapter);
    }

    private void fetchTickets() {
        TicketApi api = RetrofitClient.getTicketApi();
        api.getTickets().enqueue(new Callback<ApiResponse<List<Ticket>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Ticket>>> call, @NonNull Response<ApiResponse<List<Ticket>>> response) {
                if (binding == null) return;

                ApiResponse<List<Ticket>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    ticketList.clear();
                    ticketList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("TICKETS_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar tus tickets", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Ticket>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("TICKETS_API", "Fallo de conexión", t);
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