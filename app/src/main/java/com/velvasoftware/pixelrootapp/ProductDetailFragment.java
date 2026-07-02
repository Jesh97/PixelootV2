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

import com.velvasoftware.pixelrootapp.network.SessionManager;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;
    private int currentProductId = -1;
    private double basePrice = 0;
    private int userRoleId = 1;

    public ProductDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRoleId = SessionManager.getInstance(requireContext()).getRolId();
        if (userRoleId >= 2) {
            binding.stickyActionButtons.setVisibility(View.GONE);
            binding.lblQuantity.setVisibility(View.GONE);
            binding.containerQuantity.setVisibility(View.GONE);
        }

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
            updateDisplayedPrice();
        });

        binding.btnMinus.setOnClickListener(v -> {
            int current = Integer.parseInt(binding.txtQuantity.getText().toString());
            if (current > 1) {
                binding.txtQuantity.setText(String.valueOf(current - 1));
                updateDisplayedPrice();
            }
        });
    }

    private void updateDisplayedPrice() {
        if (binding == null) return;
        int quantity = Integer.parseInt(binding.txtQuantity.getText().toString());

        binding.txtPriceLarge.setText(com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(basePrice));
        binding.txtTotalBottom.setText(com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(basePrice * quantity));
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

        basePrice = product.getPrice();
        binding.txtOldPrice.setVisibility(View.GONE); // no hay precio de descuento real en el backend
        updateDisplayedPrice();

        binding.txtRatingValue.setText(product.getRating());

        bindReviewsSummary(product.getRatingValue());

        String description = product.getDescription();
        binding.txtDescriptionDetail.setText(
                description != null && !description.isEmpty() ? description : "Este juego todavía no tiene descripción.");

        com.velvasoftware.pixelrootapp.utils.ImageLoader.loadMainBanner(binding.imgMain, product.getImageUrl());
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

    /**
     * El backend NO tiene tabla de reseñas individuales, solo el promedio (calificacion_promedio)
     * por juego. El número grande y las estrellas SÍ son 100% reales. Las 5 barras de abajo son
     * una ESTIMACIÓN matemática a partir de ese único promedio (más alto el promedio, más
     * concentrado el "peso" en 5★/4★), no conteos reales de reseñas por estrella —
     * cuando exista una tabla real de reseñas, reemplazar esta función por un conteo real.
     */
    private void bindReviewsSummary(double average) {
        binding.reviewsSummary.txtRatingBig.setText(String.format("%.1f", average));
        binding.reviewsSummary.ratingBarBig.setRating((float) average);

        int[] percents = estimateStarDistribution(average);

        bindBarRow(binding.reviewsSummary.row5, "5", percents[0]);
        bindBarRow(binding.reviewsSummary.row4, "4", percents[1]);
        bindBarRow(binding.reviewsSummary.row3, "3", percents[2]);
        bindBarRow(binding.reviewsSummary.row2, "2", percents[3]);
        bindBarRow(binding.reviewsSummary.row1, "1", percents[4]);
    }

    private void bindBarRow(com.velvasoftware.pixelrootapp.databinding.LayoutRatingBarRowBinding row, String star, int percent) {
        row.txtStarLabel.setText(star);
        row.progressBar.setProgress(percent);
        row.txtPercent.setText(percent + "%");
    }

    /** Distribución sintética: concentra el peso alrededor del promedio real, en % que suman ~100. */
    private int[] estimateStarDistribution(double average) {
        double[] weights = new double[5]; // índice 0 = 5★ ... índice 4 = 1★
        for (int star = 5; star >= 1; star--) {
            double distancia = Math.abs(average - star);
            weights[5 - star] = Math.max(0.05, 1.0 - (distancia / 4.0));
        }
        double sum = 0;
        for (double w : weights) sum += w;

        int[] percents = new int[5];
        for (int i = 0; i < 5; i++) {
            percents[i] = (int) Math.round((weights[i] / sum) * 100);
        }
        return percents;
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