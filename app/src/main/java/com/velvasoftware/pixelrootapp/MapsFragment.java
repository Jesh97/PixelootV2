package com.velvasoftware.pixelrootapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.network.api.BranchApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";

    private GoogleMap mMap;
    private final List<Branch> branchList = new ArrayList<>();
    private Branch selectedBranch;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean mapReady = false;

    private View cardSelectedBranch;
    private TextView txtSelName, txtSelAddress;
    private Double targetLat, targetLng;

    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean fineLocationGranted = Objects.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION), true);
                boolean coarseLocationGranted = Objects.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION), true);

                if (fineLocationGranted || coarseLocationGranted) {
                    enableMyLocation();
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Permiso de ubicación denegado. No podremos mostrarte la sucursal más cercana.", Toast.LENGTH_LONG).show();
                }
            });

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            mapReady = true;

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            checkLocationPermissions();

            mMap.setOnMarkerClickListener(marker -> {
                Branch branch = (Branch) marker.getTag();
                if (branch != null) {
                    showBranchDetails(branch);
                }
                return false;
            });

            // Si las sucursales ya llegaron de la API antes de que el mapa estuviera listo,
            // recién ahora podemos pintarlas.
            if (!branchList.isEmpty()) {
                addMarkersToMap();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (getArguments() != null && getArguments().containsKey("lat") && getArguments().containsKey("lng")) {
            targetLat = getArguments().getDouble("lat");
            targetLng = getArguments().getDouble("lng");
        }

        cardSelectedBranch = view.findViewById(R.id.cardSelectedBranch);
        txtSelName = view.findViewById(R.id.txtSelName);
        txtSelAddress = view.findViewById(R.id.txtSelAddress);
        cardSelectedBranch.setVisibility(View.GONE); // se muestra recién cuando haya una sucursal real seleccionada

        view.findViewById(R.id.btnBackFromMap).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btnCallBranch).setOnClickListener(v -> {
            if (selectedBranch != null && selectedBranch.getPhone() != null && !selectedBranch.getPhone().isEmpty()) {
                contactBranch(selectedBranch.getPhone());
            } else {
                Toast.makeText(getContext(), "Esta sucursal no tiene teléfono registrado", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btnNavigate).setOnClickListener(v -> {
            if (selectedBranch != null) {
                openGoogleMapsDirections(new LatLng(selectedBranch.getLatitude(), selectedBranch.getLongitude()));
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }

        loadBranches();
    }

    private void loadBranches() {
        BranchApi api = RetrofitClient.getBranchApi();
        api.getBranches().enqueue(new Callback<ApiResponse<List<Branch>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Response<ApiResponse<List<Branch>>> response) {
                ApiResponse<List<Branch>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    branchList.clear();
                    branchList.addAll(body.getData());

                    if (mapReady) {
                        addMarkersToMap();
                    }
                } else {
                    Log.e(TAG, "No se pudieron cargar sucursales: " + response.code());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "No se pudieron cargar las sucursales", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo al cargar sucursales", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void addMarkersToMap() {
        if (mMap == null) return;

        mMap.clear();
        for (Branch branch : branchList) {
            LatLng position = new LatLng(branch.getLatitude(), branch.getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(branch.getName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if (marker != null) {
                marker.setTag(branch);
            }
        }

        // Por defecto mostramos la sucursal específica que se pidió abrir (si venimos de
        // "Ver en Mapa" de una sucursal puntual); si no, la primera de la lista mientras
        // no tengamos la ubicación del usuario.
        if (!branchList.isEmpty() && selectedBranch == null) {
            Branch toShow = findTargetBranch();
            if (toShow == null) toShow = branchList.get(0);
            showBranchDetails(toShow);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(toShow.getLatitude(), toShow.getLongitude()), 14f));
        }
    }

    /** Busca la sucursal cuyas coordenadas coincidan con las que llegaron por argumento (lat/lng). */
    @Nullable
    private Branch findTargetBranch() {
        if (targetLat == null || targetLng == null) return null;
        for (Branch branch : branchList) {
            boolean sameLat = Math.abs(branch.getLatitude() - targetLat) < 0.0001;
            boolean sameLng = Math.abs(branch.getLongitude() - targetLng) < 0.0001;
            if (sameLat && sameLng) return branch;
        }
        return null;
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
            getCurrentLocation();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    findClosestBranch(location);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Error de permisos al obtener ubicación", e);
        }
    }

    private void findClosestBranch(Location userLocation) {
        // Si el usuario entró a ver una sucursal puntual (desde "Ver en Mapa" en la lista),
        // no dejamos que la ubicación por GPS le cambie la selección.
        if (targetLat != null) return;

        if (branchList.isEmpty()) return;

        Branch closest = branchList.get(0);
        float minDistance = Float.MAX_VALUE;

        for (Branch branch : branchList) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                    branch.getLatitude(), branch.getLongitude(), results);
            if (results[0] < minDistance) {
                minDistance = results[0];
                closest = branch;
            }
        }

        showBranchDetails(closest);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(closest.getLatitude(), closest.getLongitude()), 14f));
        }
    }

    private void showBranchDetails(Branch branch) {
        selectedBranch = branch;
        txtSelName.setText(branch.getName());
        txtSelAddress.setText(branch.getAddress() + (branch.getCity() != null ? ", " + branch.getCity() : ""));
        cardSelectedBranch.setVisibility(View.VISIBLE);
    }

    private void contactBranch(String phone) {
        // Intenta abrir WhatsApp primero, si no, abre el marcador telefónico
        try {
            String url = "https://api.whatsapp.com/send?phone=+51" + phone;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+51" + phone));
            startActivity(intent);
        }
    }

    private void openGoogleMapsDirections(LatLng destination) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination.latitude + "," + destination.longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + destination.latitude + "," + destination.longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }
}