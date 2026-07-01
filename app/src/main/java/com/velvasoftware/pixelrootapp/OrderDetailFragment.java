package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.velvasoftware.pixelrootapp.databinding.FragmentOrderDetailBinding;

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
            // TODO: Implementar lógica para descargar factura
        });
    }

    private void loadOrderDetail() {
        // =========================================================================
        // BLOQUE DE DETALLE DE PEDIDO (PARA DESARROLLADOR BACKEND)
        // =========================================================================
        String orderId = getArguments() != null ? getArguments().getString("orderId") : "PR-85920";
        
        // Simulación de seteo de datos:
        binding.txtOrderIdDetail.setText("Pedido #" + orderId);
        binding.txtOrderDateDetail.setText("Realizado el 24 de Octubre, 2023");
        binding.txtOrderTotalDetail.setText("$139.97");
        binding.txtStatusBadge.setText("COMPLETADO");
        
        // Carga de items del pedido (si se usa un RecyclerView interno o contenedor dinámico)
        // =========================================================================
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
