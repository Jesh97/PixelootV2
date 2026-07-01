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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.velvasoftware.pixelrootapp.databinding.FragmentCatalogBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCategoryTabBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGameCardVerticalBinding;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.network.api.CatalogApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.network.response.GamesPageResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CatalogFragment extends Fragment {

    private FragmentCatalogBinding binding;
    private GenericAdapter<ItemGameCardVerticalBinding, Product> gamesAdapter;
    private List<Product> gameList = new ArrayList<>();

    public CatalogFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCatalogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategoryTabs();
        setupGamesGrid();

        binding.btnOpenFiltersResults.setOnClickListener(v -> showFilterBottomSheet());

        binding.etSearchCatalog.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.searchFragment)
        );
    }

    private void setupCategoryTabs() {
        List<String> tabs = new ArrayList<>();
        tabs.add("All");
        tabs.add("Action");
        tabs.add("RPG");
        tabs.add("Sci-Fi");

        binding.rvCategoryTabs.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvCategoryTabs.setAdapter(new GenericAdapter<>(tabs, ItemCategoryTabBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtTabName.setText(data);
            if ("Action".equals(data)) {
                itemBinding.getRoot().setBackgroundResource(R.drawable.bg_filter_button);
                itemBinding.txtTabName.setTextColor(getResources().getColor(R.color.negro_oscuro));
            }
        }));
    }

    private void setupGamesGrid() {
        gamesAdapter = new GenericAdapter<>(gameList, ItemGameCardVerticalBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtGameTitle.setText(data.getTitle());
            itemBinding.txtCategory.setText(data.getCategory() != null ? data.getCategory() : "");
            itemBinding.txtPrice.setText("$" + data.getPrice());
            itemBinding.txtRating.setText(data.getRating());

            // TODO: cuando se agregue una librería de imágenes (Glide/Coil), cargar data.getImageUrl()

            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
        });

        binding.rvCatalog.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvCatalog.setAdapter(gamesAdapter);

        loadGames();
    }

    private void loadGames() {
        CatalogApi api = RetrofitClient.getCatalogApi();

        api.getProducts(1).enqueue(new Callback<ApiResponse<GamesPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GamesPageResponse>> call,
                                   @NonNull Response<ApiResponse<GamesPageResponse>> response) {
                ApiResponse<GamesPageResponse> body = response.body();

                if (response.isSuccessful() && body != null && body.isStatus()
                        && body.getData() != null && body.getData().getJuegos() != null) {
                    gameList.clear();
                    gameList.addAll(body.getData().getJuegos());
                    gamesAdapter.notifyDataSetChanged();
                } else {
                    Log.e("CATALOG_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar los juegos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<GamesPageResponse>> call, @NonNull Throwable t) {
                Log.e("CATALOG_API", "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_filter_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        view.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}