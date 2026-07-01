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

import com.velvasoftware.pixelrootapp.databinding.FragmentCartBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCartProductBinding;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private GenericAdapter<ItemCartProductBinding, Product> adapter;
    private List<Product> cartItems = new ArrayList<>();

    public CartFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadCart();
        
        binding.btnCheckout.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.checkoutFragment)
        );
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(cartItems, ItemCartProductBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_cart_product.xml
            
            // 1. Título del producto en el carrito
            itemBinding.txtTitle.setText(data.getTitle());
            
            // 2. Precio unitario
            // itemBinding.txtPrice.setText("$" + data.getPrice());
            
            // 3. Cantidad y acciones (Añadir botones de +/- si existen en el layout)
            
            // 4. Imagen (Glide/Picasso)
            // Glide.with(getContext()).load(data.getImageUrl()).into(itemBinding.imgProduct);
            
            // 5. Eliminar del carrito
            // itemBinding.btnRemove.setOnClickListener(v -> { ... });
            // =========================================================================
        });

        binding.rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCartItems.setAdapter(adapter);
    }

    private void loadCart() {
        // TODO: Cargar items desde base de datos local (Room) o API
        loadMockData();
    }

    private void loadMockData() {
        cartItems.clear();
        cartItems.add(new Product(1, "Neon Abyss II", "Action", 59.99, "4.9"));
        cartItems.add(new Product(2, "Dragon Realm IV", "RPG", 44.99, "4.8"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
