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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentCategoryBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemCategoryBinding;
import com.velvasoftware.pixelrootapp.models.Category;
import com.velvasoftware.pixelrootapp.network.api.CatalogApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private GenericAdapter<ItemCategoryBinding, Category> adapter;
    private List<Category> categoryList = new ArrayList<>();

    public CategoryFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        fetchCategoriesFromApi();
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(categoryList, ItemCategoryBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Aquí se enlazan los campos del modelo con los IDs del layout 'item_category.xml'

            // 1. Título de la categoría
            itemBinding.txtCategoryName.setText(data.getName());

            // 2. Descripción de la categoría
            itemBinding.txtCategoryDescription.setText(data.getDescription());

            // 3. Icono de la categoría (Implementar Glide/Picasso para data.getIconUrl())
            // itemBinding.imgCategoryIcon.setImageResource(R.drawable.icon_default);

            // 4. Acción de navegación al hacer clic en el item
            View.OnClickListener navigateAction = v -> {
                Bundle args = new Bundle();
                args.putInt("categoryId", data.getId());
                Navigation.findNavController(v).navigate(R.id.catalogFragment, args);
            };

            itemBinding.getRoot().setOnClickListener(navigateAction);
            itemBinding.btnExploreCategory.setOnClickListener(navigateAction);
            // =========================================================================
        });

        binding.rvCategoriesList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvCategoriesList.setAdapter(adapter);
    }

    private void fetchCategoriesFromApi() {
        // =========================================================================
        // ESTRUCTURA DE LLAMADA A API (RETROFIT)
        // =========================================================================
        CatalogApi api = RetrofitClient.getCatalogApi();
        Call<ApiResponse<List<Category>>> call = api.getCategories();

        call.enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Response<ApiResponse<List<Category>>> response) {
                ApiResponse<List<Category>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    categoryList.clear();
                    categoryList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API_ERROR", "Error al cargar categorías: " + response.code());
                    loadMockData(); // Fallback a datos estáticos si falla la API
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Category>>> call, @NonNull Throwable t) {
                Log.e("API_FAILURE", t.getMessage());
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                loadMockData(); // Fallback
            }
        });
        // =========================================================================
    }

    private void loadMockData() {
        categoryList.clear();
        categoryList.add(new Category(1, "Acción", "Muestra tu destreza en juegos de adrenalina."));
        categoryList.add(new Category(2, "RPG", "Sumérgete en mundos fantásticos."));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}