package com.velvasoftware.pixelrootapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import android.util.Log;

public class MapsFragment extends Fragment {

    private static final String TAG = "MapsFragment";

    private GoogleMap mMap;
    private List<Branch> branchList;
    private Branch selectedBranch;
    private FusedLocationProviderClient fusedLocationClient;

    private View cardSelectedBranch;
    private TextView txtSelName, txtSelAddress;

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

    private static class Branch {
        String name;
        String address;
        LatLng location;
        String phone;

        Branch(String name, String address, LatLng location, String phone) {
            this.name = name;
            this.address = address;
            this.location = location;
            this.phone = phone;
        }
    }

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;
            
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            setupBranches();
            checkLocationPermissions();
            
            mMap.setOnMarkerClickListener(marker -> {
                Branch branch = (Branch) marker.getTag();
                if (branch != null) {
                    showBranchDetails(branch);
                }
                return false;
            });
        }
    };

    private void setupBranches() {
        branchList = new ArrayList<>();
        // El número proporcionado es 977329287
        branchList.add(new Branch("PixelRoot HQ - Arequipa", "Calle Mercaderes 123, Arequipa", new LatLng(-16.3988, -71.5369), "977329287"));
        branchList.add(new Branch("Sucursal Chiclayo Centro", "Av. Balta 456, Chiclayo", new LatLng(-6.7719, -79.8441), "977329287"));
        branchList.add(new Branch("Sucursal Lima Miraflores", "Av. Larco 789, Miraflores", new LatLng(-12.1227, -77.0305), "977329287"));

        for (Branch branch : branchList) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(branch.location)
                    .title(branch.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            if (marker != null) {
                marker.setTag(branch);
            }
        }

        // Por defecto mostramos la primera si no hay ubicación aún
        if (!branchList.isEmpty() && selectedBranch == null) {
            showBranchDetails(branchList.get(0));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(branchList.get(0).location, 10f));
        }
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
        if (branchList == null || branchList.isEmpty()) return;

        Branch closest = branchList.get(0);
        float minDistance = Float.MAX_VALUE;

        for (Branch branch : branchList) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(),
                    branch.location.latitude, branch.location.longitude, results);
            if (results[0] < minDistance) {
                minDistance = results[0];
                closest = branch;
            }
        }

        showBranchDetails(closest);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(closest.location, 14f));
    }

    private void showBranchDetails(Branch branch) {
        selectedBranch = branch;
        txtSelName.setText(branch.name);
        txtSelAddress.setText(branch.address);
        cardSelectedBranch.setVisibility(View.VISIBLE);
    }

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

        cardSelectedBranch = view.findViewById(R.id.cardSelectedBranch);
        txtSelName = view.findViewById(R.id.txtSelName);
        txtSelAddress = view.findViewById(R.id.txtSelAddress);

        view.findViewById(R.id.btnBackFromMap).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btnCallBranch).setOnClickListener(v -> {
            if (selectedBranch != null) {
                contactBranch(selectedBranch.phone);
            }
        });

        view.findViewById(R.id.btnNavigate).setOnClickListener(v -> {
            if (selectedBranch != null) {
                openGoogleMapsDirections(selectedBranch.location);
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private void contactBranch(String phone) {
        // Intenta abrir WhatsApp primero, si no, abre el marcador
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
