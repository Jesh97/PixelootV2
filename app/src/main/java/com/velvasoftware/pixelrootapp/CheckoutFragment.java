package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.velvasoftware.pixelrootapp.databinding.FragmentCheckoutBinding;
import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.models.CartItem;
import com.velvasoftware.pixelrootapp.network.api.BranchApi;
import com.velvasoftware.pixelrootapp.network.api.CartApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private double cartSubtotal = 0;
    private int selectedBranchId = -1;

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

        binding.btnPay.setOnClickListener(v -> goToPayment());
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

        binding.containerOrderItems.removeAllViews();
        if (cart.getItems() != null) {
            for (CartItem item : cart.getItems()) {
                TextView row = new TextView(requireContext());
                row.setTextColor(getResources().getColor(R.color.blanco_claro));
                row.setText(String.format("%dx %s — $%.2f", item.getQuantity(), item.getTitle(), item.getSubtotal()));
                row.setPadding(0, 4, 0, 4);
                binding.containerOrderItems.addView(row);
            }
        }

        // El servidor recalcula esto de nuevo al confirmar; aquí solo es para mostrarle
        // al usuario una vista previa antes de pagar.
        double igv = Math.round(cartSubtotal * 0.18 * 100) / 100.0;
        double total = cartSubtotal + igv;

        binding.txtSubtotalValue.setText(String.format("$%.2f", cartSubtotal));
        binding.txtTaxValue.setText(String.format("$%.2f", igv));
        binding.txtTotalValue.setText(String.format("$%.2f", total));
        binding.btnPay.setText(String.format("$%.2f", total));
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
        for (Branch branch : branches) {
            Chip chip = new Chip(requireContext());
            chip.setText(branch.getName());
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.negro_oscuro);
            chip.setTextColor(getResources().getColor(R.color.blanco_intermedio));
            chip.setChipStrokeColorResource(R.color.verde_oscuro_pixel);
            chip.setChipStrokeWidth(1.5f);
            chip.setTag(branch.getId());
            binding.cgBranches.addView(chip);
        }
        if (binding.cgBranches.getChildCount() > 0) {
            ((Chip) binding.cgBranches.getChildAt(0)).setChecked(true);
            selectedBranchId = (int) binding.cgBranches.getChildAt(0).getTag();
        }
        binding.cgBranches.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                View checked = group.findViewById(checkedIds.get(0));
                selectedBranchId = (int) checked.getTag();
            }
        });
    }

    private void goToPayment() {
        if (cartSubtotal <= 0) {
            Toast.makeText(getContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedBranchId <= 0) {
            Toast.makeText(getContext(), "Selecciona una sucursal de recojo", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle args = new Bundle();
        args.putInt("sucursalId", selectedBranchId);
        Navigation.findNavController(binding.btnPay).navigate(R.id.paymentFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}