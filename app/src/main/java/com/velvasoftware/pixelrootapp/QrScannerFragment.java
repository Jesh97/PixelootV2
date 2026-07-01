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

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.velvasoftware.pixelrootapp.databinding.FragmentQrScannerBinding;

import java.util.List;

public class QrScannerFragment extends Fragment {

    private FragmentQrScannerBinding binding;

    public QrScannerFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQrScannerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupScanner();
        
        binding.btnCancelScan.setOnClickListener(v -> 
            Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupScanner() {
        // =========================================================================
        // LÓGICA DE LECTURA DE QR (ZXING EMBEDDED)
        // =========================================================================
        binding.barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    binding.barcodeScanner.pause();
                    String scannedCode = result.getText();
                    
                    Toast.makeText(getContext(), "Código detectado: " + scannedCode, Toast.LENGTH_SHORT).show();

                    // PROCESAMIENTO: Pasar el resultado al fragmento anterior
                    Bundle bundle = new Bundle();
                    bundle.putString("scanned_order_id", scannedCode);
                    
                    // Volver atrás con el resultado (Simulación de setFragmentResult o similar)
                    getParentFragmentManager().setFragmentResult("qr_scan_request", bundle);
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                }
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
        });
        // =========================================================================
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.barcodeScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.barcodeScanner.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
