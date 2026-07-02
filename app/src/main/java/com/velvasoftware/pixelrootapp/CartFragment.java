package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentCartBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCartProductBinding;
import com.velvasoftware.pixelrootapp.models.CartItem;
import com.velvasoftware.pixelrootapp.network.api.CartApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.UpdateCartRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;
import com.velvasoftware.pixelrootapp.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private GenericAdapter<ItemCartProductBinding, CartItem> adapter;
    private final List<CartItem> cartItems = new ArrayList<>();

    public CartFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadCart();

        binding.btnCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
                return;
            }
            Navigation.findNavController(v).navigate(R.id.checkoutFragment);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Por si el usuario agregó/quitó productos desde el catálogo o el detalle y volvió aquí.
        loadCart();
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(cartItems, ItemCartProductBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtTitle.setText(data.getTitle());
            itemBinding.txtSubtitle.setText(CurrencyUtils.format(data.getUnitPrice()) + " c/u");
            itemBinding.txtCurrentPrice.setText(CurrencyUtils.format(data.getSubtotal()));
            itemBinding.txtQuantity.setText(String.valueOf(data.getQuantity()));

            com.velvasoftware.pixelrootapp.utils.ImageLoader.loadThumbnail(itemBinding.imgProduct, data.getImageUrl());

            itemBinding.btnPlus.setOnClickListener(v -> updateQuantity(data.getJuegoId(), data.getQuantity() + 1));

            itemBinding.btnMinus.setOnClickListener(v -> {
                int nueva = data.getQuantity() - 1;
                if (nueva <= 0) {
                    removeItem(data.getJuegoId());
                } else {
                    updateQuantity(data.getJuegoId(), nueva);
                }
            });

            itemBinding.btnRemove.setOnClickListener(v -> removeItem(data.getJuegoId()));
        });

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartItems.setAdapter(adapter);
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
                    Log.e("CART_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudo cargar el carrito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("CART_API", "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindCart(CartResponse cart) {
        cartItems.clear();
        if (cart.getItems() != null) {
            cartItems.addAll(cart.getItems());
        }
        adapter.notifyDataSetChanged();

        binding.txtBadge.setText(String.valueOf(cartItems.size()));
        binding.txtOrderTotalValue.setText(CurrencyUtils.format(cart.getSubtotal()));
    }

    private void updateQuantity(int juegoId, int nuevaCantidad) {
        CartApi api = RetrofitClient.getCartApi();
        api.updateProduct(juegoId, new UpdateCartRequest(nuevaCantidad)).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Response<ApiResponse<CartResponse>> response) {
                if (binding == null) return;
                ApiResponse<CartResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindCart(body.getData());
                } else {
                    Toast.makeText(getContext(), "No se pudo actualizar la cantidad", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void removeItem(int juegoId) {
        CartApi api = RetrofitClient.getCartApi();
        api.removeProduct(juegoId).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Response<ApiResponse<CartResponse>> response) {
                if (binding == null) return;
                ApiResponse<CartResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindCart(body.getData());
                } else {
                    Toast.makeText(getContext(), "No se pudo eliminar el producto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Throwable t) {
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