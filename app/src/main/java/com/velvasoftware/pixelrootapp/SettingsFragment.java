package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.velvasoftware.pixelrootapp.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    public SettingsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnEditProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.editProfileFragment)
        );

        binding.btnSavedAddresses.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Direcciones Guardadas - Próximamente", Toast.LENGTH_SHORT).show();
        });

        binding.btnPaymentMethods.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Medios de Pago - Próximamente", Toast.LENGTH_SHORT).show();
        });

        binding.btnPixelCredits.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Detalle de Créditos - Próximamente", Toast.LENGTH_SHORT).show();
        });

        // Switches
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Notificaciones activadas" : "Notificaciones desactivadas";
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        binding.switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Acceso a ubicación permitido" : "Acceso a ubicación denegado";
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });

        binding.switchSounds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Sonidos activados" : "Sonidos desactivados";
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}