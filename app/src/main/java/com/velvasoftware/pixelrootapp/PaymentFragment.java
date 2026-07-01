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

import com.velvasoftware.pixelrootapp.databinding.FragmentPaymentBinding;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.ConfirmOrderRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentFragment extends Fragment {

    private FragmentPaymentBinding binding;
    private int sucursalId = -1;

    public PaymentFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPaymentBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sucursalId = getArguments() != null ? getArguments().getInt("sucursalId", -1) : -1;

        binding.btnConfirmPayment.setOnClickListener(v -> confirmarPedido());
    }

    private void confirmarPedido() {
        if (sucursalId <= 0) {
            Toast.makeText(getContext(), "Falta seleccionar la sucursal", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardNumber = binding.etCardNumber.getText().toString().trim();
        String cardName = binding.etCardName.getText().toString().trim();
        String expiry = binding.etExpiryDate.getText().toString().trim();
        String cvv = binding.etCVV.getText().toString().trim();

        if (cardNumber.isEmpty() || cardName.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(getContext(), "Completa los datos de la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        // NOTA: no hay una pasarela de pago real conectada (Stripe/Culqi/etc). Estos datos
        // de tarjeta solo se validan que no estén vacíos; NO se procesan ni se envían a
        // ningún lado. Lo único real es la creación del pedido en la base de datos.
        binding.btnConfirmPayment.setEnabled(false);

        OrderApi api = RetrofitClient.getOrderApi();
        api.confirmarPedido(new ConfirmOrderRequest(sucursalId, "TARJETA")).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (binding == null) return;
                binding.btnConfirmPayment.setEnabled(true);

                ApiResponse<Order> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    Order order = body.getData();
                    Bundle args = new Bundle();
                    args.putInt("orderId", order.getOrderId());
                    args.putDouble("total", order.getTotal());
                    Navigation.findNavController(binding.getRoot()).navigate(R.id.orderConfirmationFragment, args);
                } else {
                    String message = body != null ? body.getMessage() : "No se pudo confirmar el pedido";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.btnConfirmPayment.setEnabled(true);
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