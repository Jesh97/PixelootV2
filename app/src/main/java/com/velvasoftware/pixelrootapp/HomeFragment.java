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

import com.velvasoftware.pixelrootapp.databinding.FragmentHomeBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemBannerPromoBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCategoryHomeBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGameCardBinding;
import com.velvasoftware.pixelrootapp.models.Category;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategories();
        setupPopularGames();
        setupOffersCarousel();
        setupFooter();
    }

    // =========================================================================
    // BACKEND: Juegos Populares -> GET /api/juegos/populares
    // =========================================================================
    private void setupPopularGames() {
        binding.rvPopularGames.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        RetrofitClient.getCatalogApi().getPopularProducts().enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Product> games = response.body().getData();
                    if (games == null) games = new ArrayList<>();
                    bindPopularGames(games);
                } else {
                    Log.e(TAG, "Error respuesta populares: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar los juegos populares", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión populares", t);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bindPopularGames(List<Product> games) {
        if (binding == null) return;
        binding.rvPopularGames.setAdapter(new GenericAdapter<ItemGameCardBinding, Product>(games, ItemGameCardBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtGameTitle.setText(data.getTitle());
            itemBinding.txtCategory.setText(data.getCategory() != null ? data.getCategory() : "");
            itemBinding.txtPrice.setText(String.format("$%.2f", data.getPrice()));
            itemBinding.txtRating.setText(data.getRating());

            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
        }));
    }

    // =========================================================================
    // BACKEND: Carrusel de Ofertas -> aún no existe endpoint en velva-api, queda mock
    // =========================================================================
    private void setupOffersCarousel() {
        List<String> offers = new ArrayList<>();
        offers.add("FLASH SALE: -70%");
        offers.add("WEEKEND DEAL: -50%");

        binding.rvOffersCarousel.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvOffersCarousel.setAdapter(new GenericAdapter<ItemBannerPromoBinding, String>(offers, ItemBannerPromoBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtGameTitle.setText(data);
        }));
    }

    // =========================================================================
    // BACKEND: Categorías Home -> GET /api/categorias/
    // =========================================================================
    private void setupCategories() {
        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        RetrofitClient.getCatalogApi().getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    List<Category> categories = response.body().getData();
                    if (categories == null) categories = new ArrayList<>();
                    bindCategories(categories);
                } else {
                    Log.e(TAG, "Error respuesta categorías: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión categorías", t);
            }
        });
    }

    private void bindCategories(List<Category> categories) {
        if (binding == null) return;
        binding.rvCategories.setAdapter(new GenericAdapter<ItemCategoryHomeBinding, Category>(categories, ItemCategoryHomeBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtCategoryName.setText(data.getName());
        }));
    }

    private void setupFooter() {
        binding.btnFooterTeam.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.devTeamFragment));
        binding.btnFooterAbout.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.aboutUsFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}