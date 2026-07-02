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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentSearchBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemSearchResultBinding;
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

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private FragmentSearchBinding binding;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private GenericAdapter<ItemSearchResultBinding, Product> adapter;
    private final List<Product> resultsList = new ArrayList<>();

    public SearchFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchLogic();

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(resultsList, ItemSearchResultBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtResultName.setText(data.getTitle());

            itemBinding.getRoot().setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("productId", data.getId());
                Navigation.findNavController(v).navigate(R.id.productDetailFragment, args);
            });
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

    // =========================================================================
    // BACKEND: Buscador -> GET /api/juegos/buscar?q=texto&pagina=1
    // =========================================================================
    private void performSearch(String query) {
        RetrofitClient.getCatalogApi().buscarJuegos(query, 1).enqueue(new Callback<ApiResponse<GamesPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<GamesPageResponse>> call, @NonNull Response<ApiResponse<GamesPageResponse>> response) {
                if (binding == null) return;

                ApiResponse<GamesPageResponse> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus()
                        && body.getData() != null && body.getData().getJuegos() != null) {
                    updateResults(body.getData().getJuegos());
                } else {
                    Log.e(TAG, "Error búsqueda: " + response.code());
                    updateResults(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<GamesPageResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión búsqueda", t);
                if (binding != null) updateResults(new ArrayList<>());
            }
        });
    }

    private void updateResults(List<Product> newList) {
        if (binding == null) return;
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