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
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.velvasoftware.pixelrootapp.databinding.FragmentCatalogBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCategoryTabBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGameCardVerticalBinding;
import com.velvasoftware.pixelrootapp.models.Category;
import com.velvasoftware.pixelrootapp.models.Platform;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.network.api.CatalogApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.AddToCartRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.CartResponse;
import com.velvasoftware.pixelrootapp.network.response.GamesPageResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogFragment extends Fragment {

    private FragmentCatalogBinding binding;

    private GenericAdapter<ItemGameCardVerticalBinding, Product> gamesAdapter;
    private GenericAdapter<ItemCategoryTabBinding, Category> tabsAdapter;

    /** Todos los juegos tal como vienen de la API, sin filtrar. */
    private final List<Product> allGames = new ArrayList<>();
    /** Lo que realmente se muestra en la grilla (filtrado + ordenado). */
    private final List<Product> gameList = new ArrayList<>();
    /** Categorías reales de la API + un "Todos" sintético al inicio (id = 0). */
    private final List<Category> categoryTabs = new ArrayList<>();
    /** Plataformas reales de la API (GET /api/juegos/plataformas). */
    private final List<Platform> platforms = new ArrayList<>();

    // ---- Estado de filtros (fuente de verdad, la pestaña superior y el bottom sheet lo comparten) ----
    private int selectedCategoryId = 0; // 0 = "Todos" (filtro rápido de la barra superior)
    private final Set<Integer> advancedCategoryIds = new HashSet<>(); // filtro multi-selección del bottom sheet
    private final Set<Integer> selectedPlatformIds = new HashSet<>();
    private float minPrice = 0f;
    private float maxPrice = 200f;
    private int minYear = 2015;
    private float minRating = 0f;

    // ---- Buscador en vivo ----
    private String searchQuery = "";
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private enum SortMode { POPULAR, PRECIO_ASC, RECIENTE }
    private SortMode currentSort = SortMode.POPULAR;

    public CatalogFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Si venimos de Home tocando una categoría, la dejamos preseleccionada.
        if (getArguments() != null && getArguments().containsKey("categoryId")) {
            selectedCategoryId = getArguments().getInt("categoryId");
        }

        setupCategoryTabs();
        setupGamesGrid();
        setupSortBar();
        setupSearch();

        binding.btnClearAll.setOnClickListener(v -> clearFilters());
        binding.chipActiveFilter.setOnClickListener(v -> clearFilters());

        // Solo el ícono de la lupa navega al SearchFragment; escribir en el campo filtra en vivo aquí mismo.
        binding.searchCatalogLayout.setStartIconOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.searchFragment)
        );

        loadCategories();
        loadPlatforms();
        loadGames();
    }

    // ================= BUSCADOR EN VIVO =================

    private void setupSearch() {
        binding.etSearchCatalog.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                searchRunnable = () -> {
                    searchQuery = s.toString().trim();
                    applyFiltersAndSort();
                };
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // ================= CATEGORÍAS (pestañas rápidas) =================

    private void setupCategoryTabs() {
        categoryTabs.clear();
        categoryTabs.add(new Category(0, "Todos", null)); // opción "ver todo"

        tabsAdapter = new GenericAdapter<>(categoryTabs, ItemCategoryTabBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtTabName.setText(data.getName());

            boolean selected = data.getId() == selectedCategoryId;
            if (selected) {
                itemBinding.getRoot().setBackgroundResource(R.drawable.bg_filter_button);
                itemBinding.txtTabName.setTextColor(getResources().getColor(R.color.negro_oscuro));
            } else {
                itemBinding.getRoot().setBackgroundResource(R.drawable.bg_card_gaming);
                itemBinding.txtTabName.setTextColor(getResources().getColor(R.color.blanco_intermedio));
            }

            itemBinding.getRoot().setOnClickListener(v -> {
                selectedCategoryId = data.getId();
                advancedCategoryIds.clear(); // la pestaña rápida manda sobre el filtro avanzado
                tabsAdapter.notifyDataSetChanged();
                updateActiveFilterChip();
                applyFiltersAndSort();
            });
        });

        binding.rvCategoryTabs.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvCategoryTabs.setAdapter(tabsAdapter);
    }

    private void loadCategories() {
        CatalogApi api = RetrofitClient.getCatalogApi();
        api.getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call,
                                   @NonNull Response<ApiResponse<List<Category>>> response) {
                ApiResponse<List<Category>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    while (categoryTabs.size() > 1) categoryTabs.remove(1);
                    categoryTabs.addAll(body.getData());
                    tabsAdapter.notifyDataSetChanged();
                    updateActiveFilterChip();
                    applyFiltersAndSort();
                } else {
                    Log.e("CATALOG_API", "No se pudieron cargar categorías: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                Log.e("CATALOG_API", "Fallo al cargar categorías", t);
            }
        });
    }

    private void loadPlatforms() {
        CatalogApi api = RetrofitClient.getCatalogApi();
        api.getPlatforms().enqueue(new Callback<ApiResponse<List<Platform>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Platform>>> call,
                                   @NonNull Response<ApiResponse<List<Platform>>> response) {
                ApiResponse<List<Platform>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    platforms.clear();
                    platforms.addAll(body.getData());
                } else {
                    Log.e("CATALOG_API", "No se pudieron cargar plataformas: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Platform>>> call, @NonNull Throwable t) {
                Log.e("CATALOG_API", "Fallo al cargar plataformas", t);
            }
        });
    }

    private void updateActiveFilterChip() {
        int totalActiveFilters = (selectedCategoryId != 0 ? 1 : 0)
                + advancedCategoryIds.size()
                + selectedPlatformIds.size()
                + (minRating > 0 ? 1 : 0);

        if (totalActiveFilters == 0) {
            binding.activeFiltersScroll.setVisibility(View.GONE);
            return;
        }

        String label;
        if (selectedCategoryId != 0) {
            label = categoryNameFor(selectedCategoryId);
        } else {
            label = totalActiveFilters + " filtros activos";
        }
        binding.chipActiveFilter.setText(label);
        binding.activeFiltersScroll.setVisibility(View.VISIBLE);
    }

    private void clearFilters() {
        selectedCategoryId = 0;
        advancedCategoryIds.clear();
        selectedPlatformIds.clear();
        minPrice = 0f;
        maxPrice = 200f;
        minYear = 2015;
        minRating = 0f;
        currentSort = SortMode.POPULAR;

        tabsAdapter.notifyDataSetChanged();
        highlightSort();
        updateActiveFilterChip();
        applyFiltersAndSort();
    }

    // ================= ORDEN (Popular / Reciente, barra superior) =================

    private void setupSortBar() {
        binding.txtSortPopular.setOnClickListener(v -> {
            currentSort = SortMode.POPULAR;
            highlightSort();
            applyFiltersAndSort();
        });
        binding.txtSortRecent.setOnClickListener(v -> {
            currentSort = SortMode.RECIENTE;
            highlightSort();
            applyFiltersAndSort();
        });
        highlightSort();
    }

    private void highlightSort() {
        boolean popular = currentSort == SortMode.POPULAR;
        binding.txtSortPopular.setBackgroundResource(popular ? R.drawable.bg_flash_sale : R.drawable.bg_timer_box);
        binding.txtSortPopular.setTextColor(getResources().getColor(
                popular ? R.color.verde_claro_pixel : R.color.blanco_intermedio));

        boolean reciente = currentSort == SortMode.RECIENTE;
        binding.txtSortRecent.setBackgroundResource(reciente ? R.drawable.bg_flash_sale : R.drawable.bg_timer_box);
        binding.txtSortRecent.setTextColor(getResources().getColor(
                reciente ? R.color.verde_claro_pixel : R.color.blanco_intermedio));
    }

    // ================= JUEGOS (grilla) =================

    private void setupGamesGrid() {
        gamesAdapter = new GenericAdapter<>(gameList, ItemGameCardVerticalBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtGameTitle.setText(data.getTitle());
            itemBinding.txtCategory.setText(data.getCategory() != null ? data.getCategory() : "");
            itemBinding.txtPrice.setText(com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(data.getPrice()));
            itemBinding.txtRating.setText(data.getRating());

            com.velvasoftware.pixelrootapp.utils.ImageLoader.loadGameCover(itemBinding.imgGame, data.getImageUrl());

            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });

            // El botón "+" agrega al carrito directo, sin navegar al detalle
            itemBinding.btnAddCart.setOnClickListener(v -> agregarAlCarrito(data.getId()));
        });

        binding.rvCatalog.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvCatalog.setAdapter(gamesAdapter);
    }

    // =========================================================================
    // BACKEND: Agregar al carrito -> POST /api/carrito/productos (requiere login)
    // =========================================================================
    private void agregarAlCarrito(int juegoId) {
        AddToCartRequest body = new AddToCartRequest(juegoId, 1);

        RetrofitClient.getCartApi().addProduct(body).enqueue(new Callback<ApiResponse<CartResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CartResponse>> call, @NonNull Response<ApiResponse<CartResponse>> response) {
                if (getContext() == null) return;

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
                Log.e("CATALOG_API", "Fallo conexión carrito", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadGames() {
        loadGamesPage(1);
    }

    /**
     * El backend pagina en bloques fijos de 20 (por_pagina=20, no configurable desde query).
     * Para traer TODOS los juegos, pedimos página por página hasta cubrir total_paginas
     * y vamos acumulando en allGames; el filtrado/orden se hace en el cliente.
     */
    private void loadGamesPage(int pagina) {
        CatalogApi api = RetrofitClient.getCatalogApi();

        api.getProducts(pagina).enqueue(new Callback<ApiResponse<GamesPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GamesPageResponse>> call,
                                   @NonNull Response<ApiResponse<GamesPageResponse>> response) {
                ApiResponse<GamesPageResponse> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus()
                        && body.getData() != null && body.getData().getJuegos() != null) {

                    GamesPageResponse page = body.getData();

                    if (pagina == 1) {
                        allGames.clear();
                    }
                    allGames.addAll(page.getJuegos());
                    applyFiltersAndSort();

                    if (pagina < page.getTotalPaginas()) {
                        loadGamesPage(pagina + 1);
                    }
                } else {
                    Log.e("CATALOG_API", "Respuesta no exitosa: " + response.code());
                    if (pagina == 1) {
                        Toast.makeText(getContext(), "No se pudieron cargar los juegos", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<GamesPageResponse>> call, @NonNull Throwable t) {
                Log.e("CATALOG_API", "Fallo de conexión", t);
                if (pagina == 1) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /** Aplica todos los filtros activos (rápido + avanzado + búsqueda) y el orden actual sobre allGames -> gameList. */
    private void applyFiltersAndSort() {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allGames) {
            if (selectedCategoryId != 0 && p.getCategoriaId() != selectedCategoryId) continue;
            if (!advancedCategoryIds.isEmpty() && !advancedCategoryIds.contains(p.getCategoriaId())) continue;
            if (!selectedPlatformIds.isEmpty() && !selectedPlatformIds.contains(p.getPlataformaId())) continue;
            if (p.getPrice() < minPrice || p.getPrice() > maxPrice) continue;
            if (p.getRatingValue() < minRating) continue;
            if (minYear > 2015 && !releaseYearAtLeast(p, minYear)) continue;
            if (!searchQuery.isEmpty() && (p.getTitle() == null || !p.getTitle().toLowerCase().contains(searchQuery.toLowerCase()))) continue;

            p.setCategory(categoryNameFor(p.getCategoriaId()));
            filtered.add(p);
        }

        switch (currentSort) {
            case PRECIO_ASC:
                filtered.sort(Comparator.comparingDouble(Product::getPrice));
                break;
            case RECIENTE:
                filtered.sort((a, b) -> {
                    String da = a.getReleaseDate() != null ? a.getReleaseDate() : "";
                    String db = b.getReleaseDate() != null ? b.getReleaseDate() : "";
                    return db.compareTo(da);
                });
                break;
            case POPULAR:
            default:
                filtered.sort(Comparator.comparingDouble(Product::getRatingValue).reversed());
                break;
        }

        gameList.clear();
        gameList.addAll(filtered);
        gamesAdapter.notifyDataSetChanged();
        updateResultCount(gameList.size());
    }

    private boolean releaseYearAtLeast(Product p, int year) {
        if (p.getReleaseDate() == null || p.getReleaseDate().length() < 4) return true;
        try {
            int releaseYear = Integer.parseInt(p.getReleaseDate().substring(0, 4));
            return releaseYear >= year;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private String categoryNameFor(int categoriaId) {
        for (Category c : categoryTabs) {
            if (c.getId() == categoriaId) return c.getName();
        }
        return "";
    }

    private void updateResultCount(int count) {
        if (binding != null) {
            binding.txtResultCount.setText(count + " resultados encontrados");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}