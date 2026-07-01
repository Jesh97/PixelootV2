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

import com.velvasoftware.pixelrootapp.databinding.FragmentOrdersBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemOrderHistoryBinding;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private GenericAdapter<ItemOrderHistoryBinding, Order> adapter;
    private final List<Order> orderList = new ArrayList<>();

    public OrdersFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        fetchOrders();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Por si el usuario acaba de completar una compra y vuelve a esta pantalla.
        fetchOrders();
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(orderList, ItemOrderHistoryBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtOrderId.setText("Pedido #" + data.getOrderId());
            itemBinding.txtOrderDate.setText("Fecha: " + data.getDate());
            itemBinding.txtOrderStatus.setText(data.getStatus());
            itemBinding.txtOrderTotal.setText(String.format("$%.2f", data.getTotal()));
            itemBinding.txtItemCount.setText(
                    data.getItems() != null ? data.getItems().size() + " producto(s)" : "");

            if ("PENDIENTE".equals(data.getStatus())) {
                itemBinding.txtOrderStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.amarillo_oscuro_pixel)));
                itemBinding.txtOrderStatus.setTextColor(getResources().getColor(R.color.amarillo_claro_pixel));
            } else {
                itemBinding.txtOrderStatus.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.verde_oscuro_pixel)));
                itemBinding.txtOrderStatus.setTextColor(getResources().getColor(R.color.verde_claro_pixel));
            }

            View.OnClickListener navigateAction = v -> {
                Bundle args = new Bundle();
                args.putInt("orderId", data.getOrderId());
                Navigation.findNavController(v).navigate(R.id.orderDetailFragment, args);
            };

            itemBinding.btnViewDetail.setOnClickListener(navigateAction);
            itemBinding.getRoot().setOnClickListener(navigateAction);
        });

        binding.rvOrdersHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrdersHistory.setAdapter(adapter);
    }

    private void fetchOrders() {
        OrderApi api = RetrofitClient.getOrderApi();
        api.getOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (binding == null) return;

                ApiResponse<List<Order>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    orderList.clear();
                    orderList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("ORDERS_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar tus pedidos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("ORDERS_API", "Fallo de conexión", t);
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