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

import com.velvasoftware.pixelrootapp.databinding.FragmentOrderConfirmationBinding;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmationFragment extends Fragment {

    private FragmentOrderConfirmationBinding binding;
    private Order currentOrder;

    public OrderConfirmationFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int orderId = getArguments() != null ? getArguments().getInt("orderId", -1) : -1;
        double total = getArguments() != null ? getArguments().getDouble("total", 0) : 0;

        binding.txtOrderIdConfirmation.setText("Pedido #" + orderId + " — " + com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(total));

        setupListeners(orderId);
        loadOrderDetail(orderId);
    }

    private void setupListeners(int orderId) {
        binding.btnDownloadReceipt.setOnClickListener(v -> {
            if (currentOrder != null) {
                generateOrderPdf(currentOrder);
            } else {
                Toast.makeText(getContext(), "Espera un momento, cargando el pedido...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBackHome.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.homeFragment)
        );
    }

    private void loadOrderDetail(int orderId) {
        if (orderId <= 0) return;

        OrderApi api = RetrofitClient.getOrderApi();
        api.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                ApiResponse<Order> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    currentOrder = body.getData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                // El usuario igual puede volver a Home; el recibo simplemente no se podrá generar
                // hasta que haya conexión.
            }
        });
    }

    private void generateOrderPdf(Order order) {
        com.velvasoftware.pixelrootapp.utils.OrderReceiptGenerator.generateAndOpen(this, order);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}