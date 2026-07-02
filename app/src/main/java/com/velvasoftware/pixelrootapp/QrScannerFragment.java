package com.velvasoftware.pixelrootapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.velvasoftware.pixelrootapp.databinding.FragmentQrScannerBinding;

import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.AgentConfirmRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScannerFragment extends Fragment {

    private FragmentQrScannerBinding binding;
    private boolean isConfirming = false;
    private boolean modeReturnResult = false;

    private final ActivityResultLauncher<String> cameraPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    binding.barcodeScanner.resume();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara necesario para escanear QR", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(requireView()).navigateUp();
                }
            });

    public QrScannerFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modeReturnResult = getArguments().getBoolean("mode_return_result", false);
        }
    }

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

        checkCameraPermission();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.barcodeScanner.resume();
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA);
        }
    }

    private void setupScanner() {
        binding.barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null && !isConfirming) {
                    String scannedCode = result.getText();

                    if (modeReturnResult) {
                        // Modo retorno: para el fragmento de tickets u otros
                        Bundle bundle = new Bundle();
                        bundle.putString("scanned_order_id", scannedCode);
                        getParentFragmentManager().setFragmentResult("qr_scan_request", bundle);
                        Navigation.findNavController(binding.getRoot()).navigateUp();
                    } else {
                        // Modo acción: Confirmar pedido directamente (Roles operativos)
                        isConfirming = true;
                        binding.barcodeScanner.pause();
                        confirmarPedido(scannedCode);
                    }
                }
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {}
        });
    }

    private void confirmarPedido(String codigoPedido) {
        Toast.makeText(getContext(), "Procesando código: " + codigoPedido, Toast.LENGTH_SHORT).show();

        RetrofitClient.getOrderApi().confirmarPedidoAgente(new AgentConfirmRequest(codigoPedido))
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                        if (binding == null) return;
                        
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Toast.makeText(getContext(), "¡PEDIDO COMPLETADO CON ÉXITO!", Toast.LENGTH_LONG).show();
                            Navigation.findNavController(binding.getRoot()).navigateUp();
                        } else {
                            String errorMsg;
                            switch (response.code()) {
                                case 404:
                                    errorMsg = "Código de pedido no encontrado";
                                    break;
                                case 403:
                                    errorMsg = "No tienes permisos operativos";
                                    break;
                                default:
                                    errorMsg = "Error al completar: " + response.code();
                                    break;
                            }
                            
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                            isConfirming = false;
                            binding.barcodeScanner.resume();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                        if (binding == null) return;
                        Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        isConfirming = false;
                        binding.barcodeScanner.resume();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.barcodeScanner.resume();
        }
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
