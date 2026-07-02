package com.velvasoftware.pixelrootapp;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketAttachmentsBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGalleryThumbnailBinding;
import com.velvasoftware.pixelrootapp.models.TicketImage;
import com.velvasoftware.pixelrootapp.network.DropboxUploader;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.AttachmentUrlRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketAttachmentsFragment extends Fragment {

    private static final String TAG = "TicketAttachments";

    private FragmentTicketAttachmentsBinding binding;
    private final List<TicketImage> images = new ArrayList<>();
    private GenericAdapter<ItemGalleryThumbnailBinding, TicketImage> adapter;
    private int ticketId = -1;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    subirImagen(uri);
                }
            });

    public TicketAttachmentsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketAttachmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ticketId = getArguments() != null ? getArguments().getInt("ticketId", -1) : -1;

        setupRecyclerView();

        binding.btnAddAttachment.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (ticketId > 0) {
            loadImagenes();
        } else {
            Toast.makeText(getContext(), "No se encontró el ticket", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(images, ItemGalleryThumbnailBinding::inflate, (itemBinding, data) -> {
            // TODO: cuando tengan Glide agregado, cargar data.getUrl() en itemBinding.imgThumb
            itemBinding.imgThumb.setImageResource(R.drawable.app_gradient);
        });

        binding.rvAttachments.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvAttachments.setAdapter(adapter);
    }

    private void subirImagen(Uri uri) {
        Toast.makeText(getContext(), "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        DropboxUploader.uploadFile(requireContext(), uri, new DropboxUploader.UploadCallback() {
            @Override
            public void onSuccess(String sharedUrl) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> guardarUrlEnBackend(sharedUrl));
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Error subiendo a Dropbox: " + message);
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error al subir imagen: " + message, Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void guardarUrlEnBackend(String url) {
        AttachmentUrlRequest body = new AttachmentUrlRequest(url);

        RetrofitClient.getTicketApi().subirUrlImagen(ticketId, body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(getContext(), "Imagen adjuntada", Toast.LENGTH_SHORT).show();
                    loadImagenes();
                } else {
                    Toast.makeText(getContext(), "No se pudo guardar la imagen", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo al guardar url", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadImagenes() {
        RetrofitClient.getTicketApi().getImagenes(ticketId).enqueue(new Callback<ApiResponse<List<TicketImage>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TicketImage>>> call, @NonNull Response<ApiResponse<List<TicketImage>>> response) {
                if (binding == null) return;

                ApiResponse<List<TicketImage>> resBody = response.body();
                if (response.isSuccessful() && resBody != null && resBody.isStatus() && resBody.getData() != null) {
                    images.clear();
                    images.addAll(resBody.getData());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "Error al cargar imágenes: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TicketImage>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión imágenes", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}