package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.velvasoftware.pixelrootapp.databinding.FragmentPaymentMethodsBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemSavedCardBinding;
import com.velvasoftware.pixelrootapp.models.SavedCard;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.UserApi;
import com.velvasoftware.pixelrootapp.network.request.SaveCardRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentMethodsFragment extends Fragment {

    private FragmentPaymentMethodsBinding binding;
    private GenericAdapter<ItemSavedCardBinding, SavedCard> adapter;
    private final List<SavedCard> cardList = new ArrayList<>();

    public PaymentMethodsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPaymentMethodsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddCard.setOnClickListener(v -> showAddCardBottomSheet());

        setupRecyclerView();
        loadCards();
    }

    private void showAddCardBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.layout_add_card_bottom_sheet, null);
        dialog.setContentView(sheetView);

        TextInputEditText etNumber = sheetView.findViewById(R.id.etNewCardNumber);
        TextInputEditText etHolder = sheetView.findViewById(R.id.etNewCardHolder);
        TextInputEditText etExpiry = sheetView.findViewById(R.id.etNewCardExpiry);
        com.google.android.material.chip.Chip chipVisa = sheetView.findViewById(R.id.chipNewCardVisa);
        com.google.android.material.chip.Chip chipMastercard = sheetView.findViewById(R.id.chipNewCardMastercard);
        View btnSave = sheetView.findViewById(R.id.btnSaveNewCard);

        com.google.android.material.chip.ChipGroup cgBrand = sheetView.findViewById(R.id.cgNewCardBrand);
        cgBrand.setOnCheckedStateChangeListener((group, checkedIds) -> {
            styleAddCardChip(chipVisa);
            styleAddCardChip(chipMastercard);
        });

        setupExpiryFormatting(etExpiry);

        btnSave.setOnClickListener(v -> {
            String number = etNumber.getText() != null ? etNumber.getText().toString().trim() : "";
            String holder = etHolder.getText() != null ? etHolder.getText().toString().trim() : "";
            String expiry = etExpiry.getText() != null ? etExpiry.getText().toString().trim() : "";
            String brand = chipVisa.isChecked() ? "VISA" : "MASTERCARD";

            if (number.replace(" ", "").length() < 12 || holder.isEmpty() || !expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                Toast.makeText(getContext(), "Completa correctamente todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            saveNewCard(number, holder, expiry, brand, dialog);
        });

        dialog.show();
    }

    private void styleAddCardChip(com.google.android.material.chip.Chip chip) {
        boolean checked = chip.isChecked();
        chip.setChipBackgroundColorResource(checked ? R.color.verde_claro_pixel : R.color.negro_oscuro);
        chip.setTextColor(getResources().getColor(checked ? R.color.negro_oscuro : R.color.blanco_claro));
        chip.setChipStrokeColorResource(checked ? R.color.verde_claro_pixel : R.color.verde_oscuro_pixel);
    }

    /** Mismo formateo MM/YY que en PaymentFragment (ver ese archivo para el detalle de cada regla). */
    private void setupExpiryFormatting(TextInputEditText field) {
        field.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (isFormatting) return;
                String digits = editable.toString().replaceAll("[^0-9]", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);

                String formatted;
                if (digits.isEmpty()) {
                    formatted = "";
                } else {
                    String month = digits.length() >= 2 ? digits.substring(0, 2) : digits;
                    if (month.length() == 1) {
                        formatted = (!month.equals("0") && !month.equals("1")) ? "0" + month : month;
                    } else {
                        int monthValue = Integer.parseInt(month);
                        if (monthValue == 0) month = "01";
                        else if (monthValue > 12) month = "12";
                        formatted = digits.length() <= 2 ? month : month + "/" + digits.substring(2);
                    }
                }

                isFormatting = true;
                editable.replace(0, editable.length(), formatted);
                field.setSelection(formatted.length());
                isFormatting = false;
            }
        });
    }

    private void saveNewCard(String number, String holder, String expiry, String brand, BottomSheetDialog dialog) {
        UserApi api = RetrofitClient.getUserApi();
        api.saveCard(new SaveCardRequest(holder, number, expiry, brand)).enqueue(new Callback<ApiResponse<List<SavedCard>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SavedCard>>> call, @NonNull Response<ApiResponse<List<SavedCard>>> response) {
                if (binding == null) return;

                ApiResponse<List<SavedCard>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    cardList.clear();
                    cardList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Tarjeta agregada", Toast.LENGTH_SHORT).show();
                } else {
                    String message = body != null ? body.getMessage() : "No se pudo guardar la tarjeta";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SavedCard>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(cardList, ItemSavedCardBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtCardBrandLast4.setText(data.getBrand() + " •••• " + data.getLast4());
            itemBinding.txtCardHolderExpiry.setText(data.getCardholderName() + " · Vence " + data.getExpiry());

            itemBinding.btnDeleteCard.setOnClickListener(v -> confirmDelete(data));
        });

        binding.rvSavedCards.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSavedCards.setAdapter(adapter);
    }

    private void loadCards() {
        UserApi api = RetrofitClient.getUserApi();
        api.getSavedCards().enqueue(new Callback<ApiResponse<List<SavedCard>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<SavedCard>>> call, @NonNull Response<ApiResponse<List<SavedCard>>> response) {
                if (binding == null) return;

                ApiResponse<List<SavedCard>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    cardList.clear();
                    cardList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                } else {
                    Log.e("PAYMENT_METHODS_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar tus tarjetas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<SavedCard>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("PAYMENT_METHODS_API", "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateEmptyState() {
        binding.emptyState.setVisibility(cardList.isEmpty() ? View.VISIBLE : View.GONE);
        binding.rvSavedCards.setVisibility(cardList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void confirmDelete(SavedCard card) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar tarjeta")
                .setMessage("¿Eliminar la tarjeta " + card.getBrand() + " •••• " + card.getLast4() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteCard(card))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteCard(SavedCard card) {
        UserApi api = RetrofitClient.getUserApi();
        api.deleteCard(card.getId()).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, @NonNull Response<ApiResponse<Void>> response) {
                if (binding == null) return;

                ApiResponse<Void> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus()) {
                    cardList.remove(card);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    Toast.makeText(getContext(), "Tarjeta eliminada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo eliminar la tarjeta", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                if (binding == null) return;
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