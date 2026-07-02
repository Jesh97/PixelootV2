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

        binding.btnPaymentMethods.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.paymentMethodsFragment)
        );

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

        boolean isEnglish = androidx.core.os.LocaleListCompat.getDefault().get(0) != null
                && "en".equals(androidx.core.os.LocaleListCompat.getDefault().get(0).getLanguage());
        binding.switchLanguage.setChecked(isEnglish);
        binding.txtLanguageLabel.setText(isEnglish ? R.string.settings_language_en : R.string.settings_language_es);

        binding.switchLanguage.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String tag = isChecked ? "en" : "es";
            binding.txtLanguageLabel.setText(isChecked ? R.string.settings_language_en : R.string.settings_language_es);
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(
                    androidx.core.os.LocaleListCompat.forLanguageTags(tag)
            );
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}