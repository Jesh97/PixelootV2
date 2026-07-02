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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentBranchBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemBranchCardBinding;
import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.network.api.BranchApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BranchFragment extends Fragment {

    private static final String TAG = "BranchFragment";
    private FragmentBranchBinding binding;
    private GenericAdapter<ItemBranchCardBinding, Branch> adapter;
    private final List<Branch> branchList = new ArrayList<>();
    private final List<Branch> allBranches = new ArrayList<>();

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    public BranchFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBranchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        fetchBranches();
        setupSearch();

        binding.btnMapView.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.mapsFragment)
        );

    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(branchList, ItemBranchCardBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtBranchName.setText(data.getName());
            itemBinding.txtBranchAddress.setText(data.getAddress());
            itemBinding.txtDistance.setText(data.getCity() != null ? data.getCity() : "");

            itemBinding.btnViewOnMap.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putDouble("lat", data.getLatitude());
                args.putDouble("lng", data.getLongitude());
                Navigation.findNavController(v).navigate(R.id.mapsFragment, args);
            });
        });

        binding.rvBranches.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBranches.setAdapter(adapter);
    }

    // =========================================================================
    // BACKEND: Sucursales -> GET /api/sucursales/
    // =========================================================================
    private void fetchBranches() {
        BranchApi api = RetrofitClient.getBranchApi();
        api.getBranches().enqueue(new Callback<ApiResponse<List<Branch>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Response<ApiResponse<List<Branch>>> response) {
                if (binding == null) return;

                ApiResponse<List<Branch>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    allBranches.clear();
                    allBranches.addAll(body.getData());

                    branchList.clear();
                    branchList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar las sucursales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e(TAG, "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    // =========================================================================
    // BACKEND: Buscador -> GET /api/sucursales/buscar?q=texto
    // =========================================================================
    private void setupSearch() {
        binding.etSearchBranch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                String query = s.toString().trim();

                if (query.isEmpty()) {
                    branchList.clear();
                    branchList.addAll(allBranches);
                    adapter.notifyDataSetChanged();
                    return;
                }

                searchRunnable = () -> buscarSucursales(query);
                searchHandler.postDelayed(searchRunnable, 400);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void buscarSucursales(String query) {
        RetrofitClient.getBranchApi().buscarSucursales(query).enqueue(new Callback<ApiResponse<List<Branch>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Response<ApiResponse<List<Branch>>> response) {
                if (binding == null) return;

                ApiResponse<List<Branch>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    branchList.clear();
                    branchList.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error búsqueda: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión búsqueda", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}