package com.velvasoftware.pixelrootapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * ======================================================================================
 * GUÍA DE CONFIGURACIÓN DE GOOGLE MAPS
 * ======================================================================================
 * 1. OBTENER API KEY: Ve a Google Cloud Console -> APIs & Services -> Credentials.
 * 2. HABILITAR SDK: Asegúrate de que "Maps SDK for Android" esté habilitado.
 * 3. ANDROID MANIFEST: Agrega dentro de <application>:
 *    <meta-data
 *        android:name="com.google.android.geo.API_KEY"
 *        android:value="TU_API_KEY_AQUÍ" />
 * 4. PERMISOS: Asegúrate de tener ACCESS_FINE_LOCATION y ACCESS_COARSE_LOCATION si 
 *    quieres mostrar la ubicación del usuario.
 * ======================================================================================
 */
public class MapsFragment extends Fragment {

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            // Ubicación por defecto: Arequipa, Perú
            LatLng pixelRootHQ = new LatLng(-16.409, -71.537);
            googleMap.addMarker(new MarkerOptions().position(pixelRootHQ).title("Pixel Root HQ"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pixelRootHQ, 15f));
            
            // Personalización adicional del mapa
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
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
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}
