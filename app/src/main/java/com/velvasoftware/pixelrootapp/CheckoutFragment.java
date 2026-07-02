package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.velvasoftware.pixelrootapp.databinding.FragmentCheckoutBinding;
import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.models.CartItem;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.models.SavedCard;
import com.velvasoftware.pixelrootapp.network.api.BranchApi;
import com.velvasoftware.pixelrootapp.network.api.CartApi;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.UserApi;
import com.velvasoftware.pixelrootapp.network.request.ConfirmOrderRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;
import com.velvasoftware.pixelrootapp.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private double cartSubtotal = 0;
    private int selectedBranchId = -1;
    private boolean requiresBranch = false;

    private final List<SavedCard> savedCards = new ArrayList<>();
    /** null = "usar tarjeta nueva" (por defecto); != null = id de una tarjeta guardada elegida. */
    private Integer selectedCardId = null;

    public CheckoutFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCart();
        loadBranches();
        loadSavedCards();

        binding.btnPay.setOnClickListener(v -> onPayClicked());
    }

    private void loadCart() {
        CartApi api = RetrofitClient.getCartApi();
        api.getCart().enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Response<ApiResponse<CartResponse>> response) {
                if (binding == null) return;

                ApiResponse<CartResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindCart(body.getData());
                } else {
                    Toast.makeText(getContext(), "No se pudo cargar el carrito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindCart(CartResponse cart) {
        cartSubtotal = cart.getSubtotal();

        requiresBranch = false;
        binding.containerOrderItems.removeAllViews();
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                if (!item.isDigital()) requiresBranch = true;

                TextView row = new TextView(requireContext());
                row.setTextColor(getResources().getColor(R.color.blanco_claro));
                row.setText(item.getQuantity() + "x " + item.getTitle() + " — " + CurrencyUtils.format(item.getSubtotal()));
                row.setPadding(0, 4, 0, 4);
                binding.containerOrderItems.addView(row);
            }
        }

        binding.sectionBranch.setVisibility(requiresBranch ? View.VISIBLE : View.GONE);
        if (!requiresBranch) {
            selectedBranchId = -1; // no aplica: el carrito es 100% digital, no hace falta recojo
        }

        // El servidor recalcula esto de nuevo al confirmar; aquí solo es para mostrarle
        // al usuario una vista previa antes de pagar.
        double igv = Math.round(cartSubtotal * 0.18 * 100) / 100.0;
        double total = cartSubtotal + igv;

        binding.txtSubtotalValue.setText(CurrencyUtils.format(cartSubtotal));
        binding.txtTaxValue.setText(CurrencyUtils.format(igv));
        binding.txtTotalValue.setText(CurrencyUtils.format(total));
        binding.btnPay.setText(CurrencyUtils.format(total));
    }

    private void loadBranches() {
        BranchApi api = RetrofitClient.getBranchApi();
        api.getBranches().enqueue(new Callback<ApiResponse<List<Branch>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Response<ApiResponse<List<Branch>>> response) {
                if (binding == null) return;

                ApiResponse<List<Branch>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindBranches(body.getData());
                } else {
                    Log.e("CHECKOUT_API", "No se pudieron cargar sucursales: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Throwable t) {
                Log.e("CHECKOUT_API", "Fallo al cargar sucursales", t);
            }
        });
    }

    private void bindBranches(List<Branch> branches) {
        binding.cgBranches.removeAllViews();
        selectedBranchId = -1; // nadie seleccionado hasta que el usuario toque un chip

        for (Branch branch : branches) {
            Chip chip = new Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle);
            chip.setText(branch.getName());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setTag(branch.getId());
            styleBranchChip(chip);

            chip.setOnClickListener(v -> {
                for (int i = 0; i < binding.cgBranches.getChildCount(); i++) {
                    Chip other = (Chip) binding.cgBranches.getChildAt(i);
                    other.setChecked(other == chip);
                    styleBranchChip(other);
                }
                selectedBranchId = (int) chip.getTag();
            });

            binding.cgBranches.addView(chip);
        }
    }

    private void styleBranchChip(Chip chip) {
        boolean checked = chip.isChecked();
        chip.setChipBackgroundColorResource(checked ? R.color.verde_claro_pixel : R.color.negro_oscuro);
        chip.setTextColor(getResources().getColor(checked ? R.color.negro_oscuro : R.color.blanco_intermedio));
        chip.setChipStrokeColorResource(checked ? R.color.verde_claro_pixel : R.color.verde_oscuro_pixel);
    }

    // ================= MÉTODO DE PAGO: tarjetas guardadas + tarjeta nueva =================

    private void loadSavedCards() {
        UserApi api = RetrofitClient.getUserApi();
        api.getSavedCards().enqueue(new Callback<ApiResponse<List<SavedCard>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SavedCard>>> call, @NonNull Response<ApiResponse<List<SavedCard>>> response) {
                if (binding == null) return;

                ApiResponse<List<SavedCard>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    savedCards.clear();
                    savedCards.addAll(body.getData());
                }
                buildPaymentMethodRows();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SavedCard>>> call, @NonNull Throwable t) {
                Log.e("CHECKOUT_API", "Fallo al cargar tarjetas guardadas", t);
                if (binding == null) return;
                buildPaymentMethodRows(); // igual mostramos la opción de "tarjeta nueva"
            }
        });
    }

    private void buildPaymentMethodRows() {
        binding.containerPaymentMethods.removeAllViews();

        // Por defecto: si hay tarjetas guardadas, se preselecciona la más reciente (la primera);
        // si no hay ninguna, queda seleccionada "usar tarjeta nueva".
        selectedCardId = savedCards.isEmpty() ? null : savedCards.get(0).getId();

        for (SavedCard card : savedCards) {
            binding.containerPaymentMethods.addView(
                    buildPaymentRow(
                            card.getBrand() + " •••• " + card.getLast4(),
                            card.getCardholderName() + " · Vence " + card.getExpiry(),
                            card.getId()
                    )
            );
        }

        // Fila fija: usar una tarjeta nueva (id null)
        binding.containerPaymentMethods.addView(
                buildPaymentRow("Usar una tarjeta nueva", "Ingresa los datos en el siguiente paso", null)
        );

        refreshPaymentRowsStyle();
    }

    private View buildPaymentRow(String title, String subtitle, Integer cardId) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = 12;
        card.setLayoutParams(cardParams);
        card.setRadius(getResources().getDimension(R.dimen.spacing_medium));
        card.setCardBackgroundColor(getResources().getColor(R.color.negro_intermedio));
        card.setStrokeWidth(2);
        card.setTag(cardId);

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int padding = (int) getResources().getDimension(R.dimen.spacing_medium);
        row.setPadding(padding, padding, padding, padding);

        RadioButton radio = new RadioButton(requireContext());
        radio.setClickable(false); // el click se maneja en la card completa

        LinearLayout textCol = new LinearLayout(requireContext());
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textParams.leftMargin = padding;
        textCol.setLayoutParams(textParams);

        TextView txtTitle = new TextView(requireContext());
        txtTitle.setText(title);
        txtTitle.setTextColor(getResources().getColor(R.color.blanco_claro));
        txtTitle.setTextSize(14f);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtSubtitle = new TextView(requireContext());
        txtSubtitle.setText(subtitle);
        txtSubtitle.setTextColor(getResources().getColor(R.color.blanco_intermedio));
        txtSubtitle.setTextSize(12f);

        textCol.addView(txtTitle);
        textCol.addView(txtSubtitle);

        row.addView(radio);
        row.addView(textCol);
        card.addView(row);

        card.setOnClickListener(v -> {
            selectedCardId = cardId;
            refreshPaymentRowsStyle();
        });

        return card;
    }

    private void refreshPaymentRowsStyle() {
        for (int i = 0; i < binding.containerPaymentMethods.getChildCount(); i++) {
            MaterialCardView card = (MaterialCardView) binding.containerPaymentMethods.getChildAt(i);
            Integer cardId = (Integer) card.getTag();
            boolean selected = (cardId == null && selectedCardId == null)
                    || (cardId != null && cardId.equals(selectedCardId));

            card.setStrokeColor(getResources().getColor(selected ? R.color.verde_claro_pixel : R.color.verde_oscuro_pixel));

            LinearLayout row = (LinearLayout) card.getChildAt(0);
            RadioButton radio = (RadioButton) row.getChildAt(0);
            radio.setChecked(selected);
            radio.setButtonTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.verde_claro_pixel)));
        }
    }

    // ================= PAGAR =================

    private void onPayClicked() {
        if (cartSubtotal <= 0) {
            Toast.makeText(getContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        if (requiresBranch && selectedBranchId <= 0) {
            Toast.makeText(getContext(), "Selecciona una sucursal de recojo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCardId != null) {
            // Tarjeta guardada: no hace falta pedir datos de nuevo, confirmamos el pedido directo.
            confirmOrderDirectly();
        } else {
            // Tarjeta nueva: sigue el flujo normal para capturar los datos.
            Bundle args = new Bundle();
            args.putInt("sucursalId", selectedBranchId);
            Navigation.findNavController(binding.btnPay).navigate(R.id.paymentFragment, args);
        }
    }

    private void confirmOrderDirectly() {
        binding.btnPay.setEnabled(false);

        Integer sucursalIdToSend = selectedBranchId > 0 ? selectedBranchId : null;
        OrderApi api = RetrofitClient.getOrderApi();
        api.confirmarPedido(new ConfirmOrderRequest(sucursalIdToSend, "TARJETA")).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                if (binding == null) return;
                binding.btnPay.setEnabled(true);

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
                binding.btnPay.setEnabled(true);
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