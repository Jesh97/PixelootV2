package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.velvasoftware.pixelrootapp.network.api.UserApi;
import com.velvasoftware.pixelrootapp.network.request.ConfirmOrderRequest;
import com.velvasoftware.pixelrootapp.network.request.SaveCardRequest;
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

        setupExpiryDateFormatting();
        setupCardBrandSelector();

        binding.btnConfirmPayment.setOnClickListener(v -> confirmarPedido());
    }

    private void setupCardBrandSelector() {
        binding.cgCardBrand.setOnCheckedStateChangeListener((group, checkedIds) -> {
            styleBrandChip(binding.chipVisa);
            styleBrandChip(binding.chipMastercard);
        });
    }

    private void styleBrandChip(com.google.android.material.chip.Chip chip) {
        boolean checked = chip.isChecked();
        chip.setChipBackgroundColorResource(checked ? R.color.verde_claro_pixel : R.color.negro_oscuro);
        chip.setTextColor(getResources().getColor(checked ? R.color.negro_oscuro : R.color.blanco_claro));
        chip.setChipStrokeColorResource(checked ? R.color.verde_claro_pixel : R.color.verde_oscuro_pixel);
    }

    /**
     * Formatea el campo como MM/YY mientras el usuario escribe, igual que las apps de pago reales:
     * - Si el primer dígito es 2-9 (mes de un solo dígito, ej. "6"), lo completa a "06" y avanza.
     * - Si el primer dígito es 0 o 1, espera el segundo dígito antes de decidir.
     * - Nunca deja que el mes quede fuera de 01-12 (lo recorta a 12 si se pasa).
     * - Inserta el "/" automáticamente entre mes y año.
     */
    private void setupExpiryDateFormatting() {
        binding.etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (isFormatting) return;

                String digits = editable.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);

                String formatted = formatExpiry(digits);

                isFormatting = true;
                editable.replace(0, editable.length(), formatted);
                binding.etExpiryDate.setSelection(formatted.length());
                isFormatting = false;
            }
        });
    }

    private String formatExpiry(String digits) {
        if (digits.isEmpty()) return "";

        String month = digits.length() >= 2 ? digits.substring(0, 2) : digits;

        if (month.length() == 1) {
            // Un solo dígito ingresado: si ya no puede ser un mes válido de 2 dígitos (2-9),
            // lo completamos con 0 adelante (6 -> 06) y pasamos directo al año.
            if (!month.equals("0") && !month.equals("1")) {
                month = "0" + month;
            } else {
                return month; // "0" o "1": esperamos el segundo dígito
            }
        } else {
            int monthValue = Integer.parseInt(month);
            if (monthValue == 0) {
                month = "01";
            } else if (monthValue > 12) {
                month = "12";
            }
        }

        if (digits.length() <= 2) {
            return month;
        }

        String year = digits.substring(2);
        return month + "/" + year;
    }

    private void confirmarPedido() {
        String cardNumber = binding.etCardNumber.getText().toString().trim();
        String cardName = binding.etCardName.getText().toString().trim();
        String expiry = binding.etExpiryDate.getText().toString().trim();
        String cvv = binding.etCVV.getText().toString().trim();

        if (cardNumber.isEmpty() || cardName.isEmpty() || expiry.isEmpty() || cvv.isEmpty()) {
            Toast.makeText(getContext(), "Completa los datos de la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            Toast.makeText(getContext(), "La fecha de expiración debe tener el formato MM/YY", Toast.LENGTH_SHORT).show();
            return;
        }

        // NOTA: no hay una pasarela de pago real conectada (Stripe/Culqi/etc). Estos datos
        // de tarjeta solo se validan que no estén vacíos; NO se procesan ni se envían a
        // ningún lado. Lo único real es la creación del pedido en la base de datos.
        binding.btnConfirmPayment.setEnabled(false);

        OrderApi api = RetrofitClient.getOrderApi();
        Integer sucursalIdToSend = sucursalId > 0 ? sucursalId : null;
        String codigoPedido = com.velvasoftware.pixelrootapp.utils.OrderCodeGenerator.generate();
        api.confirmarPedido(new ConfirmOrderRequest(sucursalIdToSend, "TARJETA", codigoPedido)).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (binding == null) return;
                binding.btnConfirmPayment.setEnabled(true);

                ApiResponse<Order> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {

                    if (binding.cbSaveCard.isChecked()) {
                        String brand = binding.chipVisa.isChecked() ? "VISA" : "MASTERCARD";
                        saveCardForFutureUse(cardNumber, cardName, expiry, brand);
                    }

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

    /**
     * Guarda la tarjeta para futuras compras. El número completo viaja UNA vez al servidor
     * (dentro de la misma llamada de confirmación de pago, por HTTPS) solo para que el backend
     * extraiga los últimos 4 dígitos y detecte la marca — nunca se guarda el número completo
     * ni el CVV en la base de datos (ver tarjeta_model.py). El CVV ni siquiera se envía aquí.
     */
    private void saveCardForFutureUse(String cardNumber, String cardName, String expiry, String brand) {
        UserApi api = RetrofitClient.getUserApi();
        api.saveCard(new SaveCardRequest(cardName, cardNumber, expiry, brand)).enqueue(new Callback<ApiResponse<java.util.List<com.velvasoftware.pixelrootapp.models.SavedCard>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<java.util.List<com.velvasoftware.pixelrootapp.models.SavedCard>>> call,
                                   @NonNull Response<ApiResponse<java.util.List<com.velvasoftware.pixelrootapp.models.SavedCard>>> response) {
                // No bloqueamos ni avisamos nada especial: si falla, el pedido ya se confirmó
                // igual, solo no se guardó la tarjeta para la próxima vez.
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<java.util.List<com.velvasoftware.pixelrootapp.models.SavedCard>>> call, @NonNull Throwable t) {
                // Igual, no interrumpe el flujo de compra.
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}