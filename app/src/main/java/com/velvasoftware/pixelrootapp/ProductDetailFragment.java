package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.velvasoftware.pixelrootapp.databinding.FragmentProductDetailBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGalleryThumbnailBinding;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailFragment extends Fragment {

    private FragmentProductDetailBinding binding;

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
        
        binding.btnAddToCart.setOnClickListener(v -> {
            // TODO: Implementar lógica de carrito
        });

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
        // =========================================================================
        // BLOQUE DE DETALLE DE PRODUCTO (PARA DESARROLLADOR BACKEND)
        // =========================================================================
        // Obtener ID del producto desde los argumentos
        int productId = getArguments() != null ? getArguments().getInt("productId") : -1;
        
        // Simulación de carga de datos desde API
        binding.txtGameTitle.setText("NEON ABYSS II");
        binding.txtPriceLarge.setText("$59.99");
        binding.txtTotalBottom.setText("$59.99");
        
        // El resto de campos (descripción, plataforma, etc.) se pueden mapear aquí
        // binding.txtDescriptionDetail.setText(data.getDescription());
        
        // Imagen principal
        // Glide.with(this).load(imageUrl).into(binding.imgMain);
        // =========================================================================
    }

    private void setupGallery() {
        List<String> thumbs = new ArrayList<>();
        thumbs.add("url1");
        thumbs.add("url2");

        binding.rvGallery.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvGallery.setAdapter(new GenericAdapter<>(thumbs, ItemGalleryThumbnailBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BACKEND: Galería de Imágenes del Producto
            // =========================================================================
            itemBinding.imgThumb.setImageResource(R.drawable.flyer_pixel); // Placeholder
            // Glide.with(getContext()).load(data).into(itemBinding.imgThumb);
            // =========================================================================
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
