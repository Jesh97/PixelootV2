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

import com.velvasoftware.pixelrootapp.databinding.FragmentOrdersBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemOrderHistoryBinding;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private GenericAdapter<ItemOrderHistoryBinding, Order> adapter;
    private List<Order> orderList = new ArrayList<>();

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

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(orderList, ItemOrderHistoryBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_order_history.xml
            
            // 1. ID del Pedido
            itemBinding.txtOrderId.setText("Pedido #" + data.getOrderId());
            
            // 2. Fecha de la transacción
            itemBinding.txtOrderDate.setText("Fecha: " + data.getDate());
            
            // 3. Estado del pedido (COMPLETADO, PENDIENTE, etc.)
            itemBinding.txtOrderStatus.setText(data.getStatus());
            
            // 4. Monto total
            itemBinding.txtOrderTotal.setText("$" + data.getTotal());
            
            // 5. Cantidad de productos (Añadir lógica si se requiere en el modelo)
            itemBinding.txtItemCount.setText("3 Videojuegos"); 

            // Estilización dinámica según estado
            if ("POR RECOGER".equals(data.getStatus())) {
                itemBinding.txtOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.amarillo_oscuro_pixel)));
                itemBinding.txtOrderStatus.setTextColor(getResources().getColor(R.color.amarillo_claro_pixel));
            } else {
                itemBinding.txtOrderStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.verde_oscuro_pixel)));
                itemBinding.txtOrderStatus.setTextColor(getResources().getColor(R.color.verde_claro_pixel));
            }

            // 6. Navegación al detalle del pedido
            View.OnClickListener navigateAction = v -> {
                Bundle args = new Bundle();
                args.putString("orderId", data.getOrderId());
                Navigation.findNavController(v).navigate(R.id.orderDetailFragment, args);
            };
            
            itemBinding.btnViewDetail.setOnClickListener(navigateAction);
            itemBinding.getRoot().setOnClickListener(navigateAction);
            // =========================================================================
        });

        binding.rvOrdersHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrdersHistory.setAdapter(adapter);
    }

    private void fetchOrders() {
        // TODO: Implementar llamada a API (Retrofit)
        loadMockData();
    }

    private void loadMockData() {
        orderList.clear();
        orderList.add(new Order("PR-85920", "24 Oct, 2023", "COMPLETADO", 139.97));
        orderList.add(new Order("PR-74211", "12 Sep, 2023", "POR RECOGER", 59.99));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
