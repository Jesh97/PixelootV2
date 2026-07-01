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
import androidx.recyclerview.widget.RecyclerView;

import com.velvasoftware.pixelrootapp.databinding.FragmentProductDetailBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGalleryThumbnailBinding;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.network.api.CartApi;
import com.velvasoftware.pixelrootapp.network.api.CatalogApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.AddToCartRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private int currentProductId = -1;

    public ProductDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
        setupGallery();
        loadProductDetails();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnAddToCart.setOnClickListener(v -> addToCart());

        binding.btnPlus.setOnClickListener(v -> {
            int current = Integer.parseInt(binding.txtQuantity.getText().toString());
            binding.txtQuantity.setText(String.valueOf(current + 1));
        });

        binding.btnMinus.setOnClickListener(v -> {
            int current = Integer.parseInt(binding.txtQuantity.getText().toString());
            if (current > 1) {
                binding.txtQuantity.setText(String.valueOf(current - 1));
            }
        });
    }

    private void loadProductDetails() {
        int productId = getArguments() != null ? getArguments().getInt("productId") : -1;
        currentProductId = productId;

        if (productId <= 0) {
            binding.txtDescriptionDetail.setText("No se encontró el juego.");
            return;
        }

        // La app no tiene ni "editorial" ni "reseñas" en la BD todavía, así que ocultamos
        // esos textos de ejemplo en vez de mostrar datos falsos.
        binding.txtPublisher.setVisibility(View.GONE);
        binding.txtReviewCount.setVisibility(View.GONE);

        CatalogApi api = RetrofitClient.getCatalogApi();
        api.getProductById(productId).enqueue(new Callback<ApiResponse<Product>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Product>> call, @NonNull Response<ApiResponse<Product>> response) {
                if (binding == null) return;

                ApiResponse<Product> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    bindProduct(body.getData());
                } else {
                    Log.e("PRODUCT_DETAIL_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudo cargar el juego", Toast.LENGTH_SHORT).show();
                    binding.txtDescriptionDetail.setText("No se pudo cargar la descripción.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Product>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("PRODUCT_DETAIL_API", "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                binding.txtDescriptionDetail.setText("No se pudo cargar la descripción.");
            }
        });
    }

    private void bindProduct(Product product) {
        binding.txtGameTitle.setText(product.getTitle());

        binding.txtPriceLarge.setText(String.format("$%.2f", product.getPrice()));
        binding.txtTotalBottom.setText(String.format("$%.2f", product.getPrice()));
        binding.txtOldPrice.setVisibility(View.GONE); // no hay precio de descuento real en el backend

        binding.txtRatingValue.setText(product.getRating());

        String description = product.getDescription();
        binding.txtDescriptionDetail.setText(
                description != null && !description.isEmpty() ? description : "Este juego todavía no tiene descripción.");

        // TODO: cuando se agregue una librería de imágenes, cargar product.getImageUrl() en imgMain
    }

    private void addToCart() {
        if (currentProductId <= 0) return;

        int cantidad = Integer.parseInt(binding.txtQuantity.getText().toString());
        binding.btnAddToCart.setEnabled(false);

        CartApi api = RetrofitClient.getCartApi();
        api.addProduct(new AddToCartRequest(currentProductId, cantidad)).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Response<ApiResponse<CartResponse>> response) {
                if (binding == null) return;
                binding.btnAddToCart.setEnabled(true);

                ApiResponse<CartResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus()) {
                    Toast.makeText(getContext(), "Añadido al carrito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo añadir al carrito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.btnAddToCart.setEnabled(true);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupGallery() {
        List<String> thumbs = new ArrayList<>();
        thumbs.add("url1");
        thumbs.add("url2");

        binding.rvGallery.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvGallery.setAdapter(new GenericAdapter<>(thumbs, ItemGalleryThumbnailBinding::inflate, (itemBinding, data) -> {
            // TODO: el backend todavía no tiene galería de imágenes por juego (solo imagen_portada
            // e imagen_banner). Cuando se agregue, reemplazar este placeholder.
            itemBinding.imgThumb.setImageResource(R.drawable.flyer_pixel);
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}