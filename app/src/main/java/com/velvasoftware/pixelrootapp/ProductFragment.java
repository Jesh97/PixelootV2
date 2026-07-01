package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentProductBinding;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;

    public ProductFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupUI();
        setupListeners();
        loadProducts();
    }

    private void setupUI() {
        binding.rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void setupListeners() {
        binding.btnOpenFilters.setOnClickListener(v -> {
            // TODO: Implement filters
        });
    }

    private void loadProducts() {
        // TODO: Load products from API
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
