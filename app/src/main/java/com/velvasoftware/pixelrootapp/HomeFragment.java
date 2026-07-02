package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.velvasoftware.pixelrootapp.network.response.GamesPageResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.velvasoftware.pixelrootapp.network.request.AddToCartRequest;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    // Copia completa de los juegos populares, para poder restaurarla cuando se borra la búsqueda
    private final List<Product> allPopularGames = new ArrayList<>();

    // Debounce del buscador
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

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
        setupSearch();
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

            // NUEVO: el botón "+" del carrito agrega directo, sin navegar al detalle
            itemBinding.btnAddCartHome.setOnClickListener(v -> agregarAlCarrito(data.getId()));
        }));
    }

    // =========================================================================
// BACKEND: Agregar al carrito -> POST /api/carrito/productos (requiere login)
// =========================================================================
    private void agregarAlCarrito(int juegoId) {
        AddToCartRequest body = new AddToCartRequest(juegoId, 1);

        RetrofitClient.getCartApi().addProduct(body).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Response<ApiResponse<CartResponse>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(getContext(), "Añadido al carrito", Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    Toast.makeText(getContext(), "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo agregar al carrito", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión carrito", t);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // Guarda la copia "maestra" SOLO cuando viene de la carga inicial de populares
    private void setPopularGamesAsMaster(List<Product> games) {
        allPopularGames.clear();
        allPopularGames.addAll(games);
        bindPopularGames(games);
    }

    // =========================================================================
    // BACKEND: Buscador -> GET /api/juegos/buscar?q=texto&pagina=1
    // =========================================================================
    private void setupSearch() {
        binding.etSearchHome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                String query = s.toString().trim();

                if (query.isEmpty()) {
                    bindPopularGames(allPopularGames);
                    return;
                }

                // Debounce: espera 400ms sin que el usuario escriba antes de llamar al API
                searchRunnable = () -> buscarJuegos(query);
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buscarJuegos(String query) {
        RetrofitClient.getCatalogApi().buscarJuegos(query, 1).enqueue(new Callback<ApiResponse<GamesPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GamesPageResponse>> call, @NonNull Response<ApiResponse<GamesPageResponse>> response) {
                if (!isAdded() || binding == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    GamesPageResponse page = response.body().getData();
                    List<Product> resultados = (page != null && page.getJuegos() != null) ? page.getJuegos() : new ArrayList<>();
                    bindPopularGames(resultados);
                } else {
                    Log.e(TAG, "Error búsqueda: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<GamesPageResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión búsqueda", t);
            }
        });
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
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}