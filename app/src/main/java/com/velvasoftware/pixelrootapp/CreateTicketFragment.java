package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.velvasoftware.pixelrootapp.databinding.FragmentCreateTicketBinding;

public class CreateTicketFragment extends Fragment {

    private FragmentCreateTicketBinding binding;

    public CreateTicketFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateTicketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupTypeSelector();
        setupQrScanner();
        setupFormValidation();
        setupListeners();
    }

    private void setupListeners() {
        binding.btnSubmitTicket.setOnClickListener(v -> {
            // =========================================================================
            // BLOQUE DE ENVÍO DE TICKET (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            String orderId = binding.etRelatedOrder.getText().toString();
            String type = binding.autoCompleteType.getText().toString();
            String description = binding.etDescription.getText().toString();
            
            // TODO: Enviar datos a la API de Soporte
            Navigation.findNavController(v).navigateUp();
            // =========================================================================
        });
    }

    private void setupTypeSelector() {
        String[] types = new String[]{"Problema con pedido", "Error en pago", "Consulta técnica", "Otro"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, types);
        binding.autoCompleteType.setAdapter(adapter);
    }

    private void setupQrScanner() {
        binding.btnScanQr.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.qrScannerFragment)
        );

        // Escuchar el resultado del escáner QR
        getParentFragmentManager().setFragmentResultListener("qr_scan_request", getViewLifecycleOwner(), (requestKey, result) -> {
            String scannedValue = result.getString("scanned_order_id");
            if (scannedValue != null) {
                binding.etRelatedOrder.setText(scannedValue);
                validateForm();
            }
        });
    }

    private void setupFormValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etRelatedOrder.addTextChangedListener(watcher);
        binding.etDescription.addTextChangedListener(watcher);
    }

    private void validateForm() {
        boolean isOrderValid = binding.etRelatedOrder.getText() != null && binding.etRelatedOrder.getText().length() > 0;
        boolean isDescValid = binding.etDescription.getText() != null && binding.etDescription.getText().length() > 5;
        
        binding.btnSubmitTicket.setEnabled(isOrderValid && isDescValid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
