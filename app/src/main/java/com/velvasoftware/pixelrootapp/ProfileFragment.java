package com.velvasoftware.pixelrootapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.velvasoftware.pixelrootapp.databinding.FragmentProfileBinding;
import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.SessionManager;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.UserApi;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupButtons();
        loadUserData();
    }

    private void loadUserData() {
        // Mientras llega la respuesta de la API, mostramos lo que ya tenemos guardado
        // localmente desde el login (evita que se vea vacío un instante).
        SessionManager session = SessionManager.getInstance(requireContext());
        String fallbackName = (session.getNombre() + " " + session.getApellido()).trim();
        binding.txtProfileName.setText(fallbackName.isEmpty() ? "Usuario" : fallbackName);
        binding.txtProfileEmail.setText(session.getCorreo());

        UserApi api = RetrofitClient.getUserApi();
        api.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (binding == null) return; // el fragment pudo destruirse antes de que llegue la respuesta

                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    User user = body.getData();
                    binding.txtProfileName.setText(user.getFullName());
                    binding.txtProfileEmail.setText(user.getEmail());
                    binding.txtProfilePhone.setText(
                            user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Sin teléfono registrado");
                    binding.txtProfileLocation.setText(
                            user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Sin dirección registrada");
                    // TODO: cuando se agregue una librería de imágenes, cargar user.getProfileImageUrl() en imgProfile
                } else {
                    Log.e("PROFILE_API", "Respuesta no exitosa: " + response.code());
                    Toast.makeText(getContext(), "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("PROFILE_API", "Fallo de conexión", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupButtons() {
        binding.btnEditProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.settingsFragment)
        );

        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.app_dialog_logout, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancelLogout);
        MaterialButton btnAccept = dialogView.findViewById(R.id.btnAcceptLogout);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAccept.setOnClickListener(v -> {
            dialog.dismiss();
            SessionManager.getInstance(requireContext()).clear();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}