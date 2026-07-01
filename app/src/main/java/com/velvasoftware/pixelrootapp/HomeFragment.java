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

import com.velvasoftware.pixelrootapp.databinding.FragmentHomeBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemBannerPromoBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCategoryHomeBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGameCardBinding;
import com.velvasoftware.pixelrootapp.models.Category;
import com.velvasoftware.pixelrootapp.models.Product;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

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

    private void setupPopularGames() {
        List<Product> games = new ArrayList<>();
        games.add(new Product(3, "GOD OF WAR", "Action", 59.99, "★ 4.9"));
        games.add(new Product(4, "STRAY", "Adventure", 29.99, "★ 4.7"));

        binding.rvPopularGames.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvPopularGames.setAdapter(new GenericAdapter<ItemGameCardBinding, Product>(games, ItemGameCardBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BACKEND: Juegos Populares
            // =========================================================================
            itemBinding.txtGameTitle.setText(data.getTitle());
            itemBinding.txtCategory.setText(data.getCategory());
            itemBinding.txtPrice.setText("$" + data.getPrice());
            itemBinding.txtRating.setText(data.getRating());
            
            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
            // =========================================================================
        }));
    }

    private void setupOffersCarousel() {
        List<String> offers = new ArrayList<>();
        offers.add("FLASH SALE: -70%");
        offers.add("WEEKEND DEAL: -50%");

        binding.rvOffersCarousel.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvOffersCarousel.setAdapter(new GenericAdapter<ItemBannerPromoBinding, String>(offers, ItemBannerPromoBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BACKEND: Carrusel de Ofertas
            // =========================================================================
            itemBinding.txtGameTitle.setText(data);
            // =========================================================================
        }));
    }

    private void setupCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(1, "Action", "Juegos de adrenalina"));
        categories.add(new Category(2, "RPG", "Mundos fantásticos"));

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        binding.rvCategories.setAdapter(new GenericAdapter<ItemCategoryHomeBinding, Category>(categories, ItemCategoryHomeBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BACKEND: Categorías Home
            // =========================================================================
            itemBinding.txtCategoryName.setText(data.getName());
            // =========================================================================
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
