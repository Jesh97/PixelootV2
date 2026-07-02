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
import com.velvasoftware.pixelrootapp.network.api.CatalogApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;
import com.velvasoftware.pixelrootapp.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.velvasoftware.pixelrootapp.network.SessionManager;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private int userRoleId = 1;

    private GenericAdapter<ItemCategoryHomeBinding, Category> categoriesAdapter;
    private GenericAdapter<ItemGameCardBinding, Product> popularGamesAdapter;

    private final List<Category> categories = new ArrayList<>();
    /** Todos los juegos populares tal como vienen de la API, sin filtrar. */
    private final List<Product> allPopularGames = new ArrayList<>();
    /** Lo que realmente se muestra (filtrado por categoría si hay alguna elegida). */
    private final List<Product> popularGames = new ArrayList<>();

    private int selectedCategoryId = 0; // 0 = ninguna categoría elegida (mostrar todos)
    private static final double DELUXE_SURCHARGE = 20.0; // debe coincidir con ProductDetailFragment

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userRoleId = SessionManager.getInstance(requireContext()).getRolId();

        setupCategories();
        setupPopularGames();
        setupOffersCarousel();
        setupFooter();
    }

    // ================= CATEGORÍAS (Explorar) =================

    private void setupCategories() {
        categoriesAdapter = new GenericAdapter<>(categories, ItemCategoryHomeBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtCategoryName.setText(data.getName());
            styleCategoryItem(itemBinding, data.getId() == selectedCategoryId);

            itemBinding.getRoot().setOnClickListener(v -> {
                // Tocar la misma categoría otra vez la deselecciona (vuelve a mostrar todos).
                selectedCategoryId = (data.getId() == selectedCategoryId) ? 0 : data.getId();
                categoriesAdapter.notifyDataSetChanged();
                applyPopularGamesFilter();
            });
        });

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvCategories.setAdapter(categoriesAdapter);

        CatalogApi api = RetrofitClient.getCatalogApi();
        api.getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                if (binding == null) return;

                ApiResponse<List<Category>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    categories.clear();
                    categories.addAll(body.getData());
                    categoriesAdapter.notifyDataSetChanged();
                } else {
                    Log.e("HOME_API", "No se pudieron cargar categorías: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                Log.e("HOME_API", "Fallo al cargar categorías", t);
            }
        });
    }

    private void styleCategoryItem(ItemCategoryHomeBinding itemBinding, boolean selected) {
        itemBinding.viewCategoryBg.setBackgroundResource(selected ? R.drawable.bg_filter_button : R.drawable.bg_card_gaming);
        itemBinding.imgCategoryIcon.setColorFilter(getResources().getColor(selected ? R.color.negro_oscuro : R.color.verde_claro_pixel));
        itemBinding.txtCategoryName.setTextColor(getResources().getColor(selected ? R.color.verde_claro_pixel : R.color.blanco_intermedio));
    }

    // ================= JUEGOS POPULARES =================

    private void setupPopularGames() {
        popularGamesAdapter = new GenericAdapter<>(popularGames, ItemGameCardBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtGameTitle.setText(data.getTitle());
            itemBinding.txtCategory.setText(data.getCategory() != null ? data.getCategory() : "");
            itemBinding.txtPrice.setText(CurrencyUtils.format(data.getPrice()));
            itemBinding.txtRating.setText(data.getRating());

            // Roles operativos (Agente, Admin, SuperAdmin) no pueden comprar
            if (userRoleId >= 2) {
                itemBinding.btnAddCartHome.setVisibility(View.GONE);
            }

            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
        });

        binding.rvPopularGames.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvPopularGames.setAdapter(popularGamesAdapter);

        CatalogApi api = RetrofitClient.getCatalogApi();
        api.getPopularProducts().enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (binding == null) return;

                ApiResponse<List<Product>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    allPopularGames.clear();
                    allPopularGames.addAll(body.getData());
                    applyPopularGamesFilter();
                } else {
                    Log.e("HOME_API", "No se pudieron cargar juegos populares: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e("HOME_API", "Fallo al cargar juegos populares", t);
                if (binding != null) {
                    Toast.makeText(getContext(), "No se pudieron cargar los juegos populares", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** Filtra allPopularGames por selectedCategoryId (0 = sin filtro) y refresca la grilla. */
    private void applyPopularGamesFilter() {
        popularGames.clear();
        for (Product p : allPopularGames) {
            if (selectedCategoryId == 0 || p.getCategoriaId() == selectedCategoryId) {
                popularGames.add(p);
            }
        }
        popularGamesAdapter.notifyDataSetChanged();
    }

    private void setupOffersCarousel() {
        List<Product> deluxeGames = new ArrayList<>();

        GenericAdapter<ItemBannerPromoBinding, Product> adapter = new GenericAdapter<>(deluxeGames, ItemBannerPromoBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtGameTitle.setText(data.getTitle());
            itemBinding.txtOldPrice.setText(CurrencyUtils.format(data.getPrice()));
            itemBinding.txtPrice.setText(CurrencyUtils.format(data.getPrice() + DELUXE_SURCHARGE));

            if (userRoleId == 2) {
                itemBinding.btnAddToCartBanner.setVisibility(View.GONE);
            }

            View.OnClickListener openDeluxeDetail = v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                args.putString("edition", "DELUXE");
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            };

            itemBinding.getRoot().setOnClickListener(openDeluxeDetail);
            itemBinding.btnAddToCartBanner.setOnClickListener(openDeluxeDetail); // el recargo Deluxe se confirma en el detalle, no se agrega a ciegas desde aquí
        });

        binding.rvOffersCarousel.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvOffersCarousel.setAdapter(adapter);

        CatalogApi api = RetrofitClient.getCatalogApi();
        api.getFeaturedProducts().enqueue(new Callback<ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Response<ApiResponse<List<Product>>> response) {
                if (binding == null) return;

                ApiResponse<List<Product>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    deluxeGames.clear();
                    deluxeGames.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("HOME_API", "No se pudieron cargar ediciones deluxe: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Product>>> call, @NonNull Throwable t) {
                Log.e("HOME_API", "Fallo al cargar ediciones deluxe", t);
            }
        });
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