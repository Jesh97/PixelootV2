package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.velvasoftware.pixelrootapp.databinding.FragmentOrderDetailBinding;
import com.velvasoftware.pixelrootapp.models.CartItem;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailFragment extends Fragment {

    private FragmentOrderDetailBinding binding;

    public OrderDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
        loadOrderDetail();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnDownloadInvoice.setOnClickListener(v -> {
            // TODO: reutilizar la generación de PDF de OrderConfirmationFragment con estos mismos datos
            Toast.makeText(getContext(), "Descarga de factura próximamente", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadOrderDetail() {
        int orderId = getArguments() != null ? getArguments().getInt("orderId", -1) : -1;

        if (orderId <= 0) {
            binding.txtOrderIdDetail.setText("Pedido no encontrado");
            return;
        }

        OrderApi api = RetrofitClient.getOrderApi();
        api.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (binding == null) return;

                ApiResponse<Order> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindOrder(body.getData());
                } else {
                    Log.e("ORDER_DETAIL_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudo cargar el pedido", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("ORDER_DETAIL_API", "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindOrder(Order order) {
        binding.txtOrderIdDetail.setText("Pedido #" + order.getOrderId());
        binding.txtOrderDateDetail.setText("Realizado el " + order.getDate());
        binding.txtOrderTotalDetail.setText(String.format("$%.2f", order.getTotal()));
        binding.txtStatusBadge.setText(order.getStatus());

        binding.containerOrderItems.removeAllViews();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                TextView row = new TextView(requireContext());
                row.setTextColor(getResources().getColor(R.color.blanco_claro));
                row.setText(String.format("%dx %s — $%.2f", item.getQuantity(), item.getTitle(), item.getSubtotal()));
                row.setPadding(0, 8, 0, 8);
                binding.containerOrderItems.addView(row);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}