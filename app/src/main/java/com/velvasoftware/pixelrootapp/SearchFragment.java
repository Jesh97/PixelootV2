package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentSearchBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemSearchResultBinding;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private GenericAdapter<ItemSearchResultBinding, String> adapter;
    private List<String> resultsList = new ArrayList<>();
    private final List<String> allItems = new ArrayList<>();

    public SearchFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupMockData();
        setupRecyclerView();
        setupSearchLogic();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupMockData() {
        allItems.add("Elden Ring");
        allItems.add("Cyberpunk 2077");
        allItems.add("God of War Ragnarök");
        allItems.add("Stray");
        allItems.add("Halo Infinite");
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(resultsList, ItemSearchResultBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_search_result.xml
            
            // 1. Nombre del resultado (Juego, Consola, etc.)
            itemBinding.txtResultName.setText(data);
            
            // 2. Acción al seleccionar resultado
            itemBinding.getRoot().setOnClickListener(v -> {
                // Navegar al detalle o aplicar búsqueda
            });
            // =========================================================================
        });

        binding.rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSearchResults.setAdapter(adapter);
    }

    private void setupSearchLogic() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    updateResults(new ArrayList<>());
                    return;
                }
                searchRunnable = () -> performSearch(query);
                searchHandler.postDelayed(searchRunnable, 500);
            }
        });
    }

    private void performSearch(String query) {
        List<String> results = new ArrayList<>();
        for (String item : allItems) {
            if (item.toLowerCase().contains(query.toLowerCase())) {
                results.add(item);
            }
        }
        updateResults(results);
    }

    private void updateResults(List<String> newList) {
        resultsList.clear();
        resultsList.addAll(newList);
        adapter.notifyDataSetChanged();
        binding.layoutNoResults.setVisibility(resultsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}
