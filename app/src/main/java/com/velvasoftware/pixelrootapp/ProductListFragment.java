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

import com.velvasoftware.pixelrootapp.databinding.FragmentProductListBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGameCardVerticalBinding;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductListFragment extends Fragment {

    private FragmentProductListBinding binding;
    private GenericAdapter<ItemGameCardVerticalBinding, Product> adapter;
    private List<Product> productList = new ArrayList<>();

    public ProductListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        setupProductList();
        loadProducts();
    }

    private void setupProductList() {
        adapter = new GenericAdapter<>(productList, ItemGameCardVerticalBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_game_card_vertical.xml

            // 1. Título del juego
            itemBinding.txtGameTitle.setText(data.getTitle());

            // 2. Categoría / Género
            itemBinding.txtCategory.setText(data.getCategory());

            // 3. Precio formateado
            itemBinding.txtPrice.setText(com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(data.getPrice()));

            // 4. Calificación (Estrellas)
            itemBinding.txtRating.setText(data.getRating());

            // 5. Plataforma (PS5, Xbox, PC, etc.)
            itemBinding.txtPlatform.setText("PS5"); // Placeholder, añadir a modelo si es necesario

            // 6. Imagen del producto (Implementar Glide/Picasso)
            // Glide.with(getContext()).load(data.getImageUrl()).into(itemBinding.imgGame);

            // 7. Navegación al detalle
            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
            // =========================================================================
        });

        binding.rvProductsList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvProductsList.setAdapter(adapter);
    }

    private void loadProducts() {
        // TODO: Implementar llamada a CatalogApi.getProducts()
        loadMockData();
    }

    private void loadMockData() {
        productList.clear();
        productList.add(new Product(1, "ELDEN RING", "Action / RPG", 59.99, "★ 4.9"));
        productList.add(new Product(2, "CYBERPUNK 2077", "RPG", 49.99, "★ 4.5"));
        productList.add(new Product(3, "MARVEL'S SPIDER-MAN 2", "Action", 69.99, "★ 4.9"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}