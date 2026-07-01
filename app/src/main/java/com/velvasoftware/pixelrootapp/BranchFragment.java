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

import com.velvasoftware.pixelrootapp.databinding.FragmentBranchBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemBranchCardBinding;
import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class BranchFragment extends Fragment {

    private FragmentBranchBinding binding;
    private GenericAdapter<ItemBranchCardBinding, Branch> adapter;
    private List<Branch> branchList = new ArrayList<>();

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
        
        binding.btnMapView.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.mapsFragment)
        );

        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && checkedId == R.id.btnMapView) {
                Navigation.findNavController(view).navigate(R.id.mapsFragment);
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(branchList, ItemBranchCardBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_branch_card.xml
            
            // 1. Nombre de la sucursal
            itemBinding.txtBranchName.setText(data.getName());
            
            // 2. Dirección física
            itemBinding.txtBranchAddress.setText(data.getAddress());
            
            // 3. Distancia calculada
            itemBinding.txtDistance.setText(data.getDistance() + " de distancia");
            
            // 4. Acción al pulsar "Ver en Mapa"
            itemBinding.btnViewOnMap.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putDouble("lat", data.getLatitude());
                args.putDouble("lng", data.getLongitude());
                Navigation.findNavController(v).navigate(R.id.mapsFragment, args);
            });
            // =========================================================================
        });

        binding.rvBranches.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvBranches.setAdapter(adapter);
    }

    private void fetchBranches() {
        // TODO: Implementar llamada a API de Sucursales
        loadMockData();
    }

    private void loadMockData() {
        branchList.clear();
        branchList.add(new Branch(1, "PixelHouse Centro", "C. Elías Aguirre 123", "0.5 km"));
        branchList.add(new Branch(2, "PixelHouse Norte", "Av. Bolognesi 2342", "1.2 km"));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
