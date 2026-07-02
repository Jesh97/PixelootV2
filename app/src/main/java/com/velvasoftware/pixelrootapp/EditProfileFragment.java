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

import com.velvasoftware.pixelrootapp.databinding.FragmentEditProfileBinding;
import com.velvasoftware.pixelrootapp.models.User;
import com.velvasoftware.pixelrootapp.network.SessionManager;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.UserApi;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;

    public EditProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        // Mientras llega la respuesta de la API, mostramos lo que ya tenemos en sesión
        // para que los campos no se vean vacíos un instante.
        SessionManager session = SessionManager.getInstance(requireContext());
        binding.txtEmailReadOnly.setText(session.getCorreo());
        binding.etFirstName.setText(session.getNombre());
        binding.etLastName.setText(session.getApellido());

        UserApi api = RetrofitClient.getUserApi();
        api.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (binding == null) return;

                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    User user = body.getData();
                    binding.txtEmailReadOnly.setText(user.getEmail());
                    binding.etFirstName.setText(user.getFirstName());
                    binding.etLastName.setText(user.getLastName());
                    binding.etPhone.setText(user.getPhone());
                    binding.etAddress.setText(user.getAddress());
                } else {
                    Toast.makeText(getContext(), "No se pudo cargar tu perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(getContext(), "El nombre y apellido no pueden estar vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        User updated = new User();
        updated.setFirstName(firstName);
        updated.setLastName(lastName);
        updated.setPhone(phone);
        updated.setAddress(address);

        binding.btnSaveProfile.setEnabled(false);

        UserApi api = RetrofitClient.getUserApi();
        api.updateProfile(updated).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<User>> call, @NonNull Response<ApiResponse<User>> response) {
                if (binding == null) return;
                binding.btnSaveProfile.setEnabled(true);

                ApiResponse<User> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    User user = body.getData();

                    // Mantenemos la sesión local sincronizada con lo que acabamos de guardar,
                    // para que el header del menú y "Mi Perfil" reflejen el cambio al instante.
                    SessionManager session = SessionManager.getInstance(requireContext());
                    session.saveSession(
                            session.getToken(),
                            session.getUsuarioId(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.getEmail() != null ? user.getEmail() : session.getCorreo(),
                            session.getRolId(),
                            session.isRememberMe()
                    );

                    Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                } else {
                    String message = body != null ? body.getMessage() : "No se pudo actualizar el perfil";
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<User>> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.btnSaveProfile.setEnabled(true);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}