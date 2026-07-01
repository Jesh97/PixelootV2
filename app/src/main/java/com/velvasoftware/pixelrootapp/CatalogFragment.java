package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

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
            // Lógica de selección visual
            if ("Action".equals(data)) {
                itemBinding.getRoot().setBackgroundResource(R.drawable.bg_filter_button);
                itemBinding.txtTabName.setTextColor(getResources().getColor(R.color.negro_oscuro));
            }
        }));
    }

    private void setupGamesGrid() {
        gamesAdapter = new GenericAdapter<>(gameList, ItemGameCardVerticalBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_game_card_vertical.xml
            
            // 1. Título del juego
            itemBinding.txtGameTitle.setText(data.getTitle());
            
            // 2. Género / Categoría
            itemBinding.txtCategory.setText(data.getCategory());
            
            // 3. Precio formateado
            itemBinding.txtPrice.setText("$" + data.getPrice());
            
            // 4. Puntuación
            itemBinding.txtRating.setText(data.getRating());

            // 5. Navegación al detalle
            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
            // =========================================================================
        });

        binding.rvCatalog.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvCatalog.setAdapter(gamesAdapter);
        
        loadGames();
    }

    private void loadGames() {
        // TODO: Llamar a CatalogApi
        loadMockData();
    }

    private void loadMockData() {
        gameList.clear();
        gameList.add(new Product(1, "Neon Abyss II", "Action / RPG", 59.99, "★ 4.9"));
        gameList.add(new Product(2, "Dragon Realm IV", "RPG / Fantasy", 44.99, "★ 4.8"));
        gamesAdapter.notifyDataSetChanged();
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
