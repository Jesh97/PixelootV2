package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketAttachmentsBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemGalleryThumbnailBinding;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class TicketAttachmentsFragment extends Fragment {

    private FragmentTicketAttachmentsBinding binding;

    public TicketAttachmentsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketAttachmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAttachments();
    }

    private void setupAttachments() {
        List<String> mockUrls = new ArrayList<>();
        mockUrls.add("url1");
        mockUrls.add("url2");

        binding.rvAttachments.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvAttachments.setAdapter(new GenericAdapter<>(mockUrls, ItemGalleryThumbnailBinding::inflate, (itemBinding, data) -> {
            // =========================================================================
            // BLOQUE DE VINCULACIÓN DE DATOS (PARA DESARROLLADOR BACKEND)
            // =========================================================================
            // Mapeo de controles del item_gallery_thumbnail.xml
            
            // 1. Imagen adjunta (Implementar Glide/Picasso)
            // Glide.with(getContext()).load(data).into(itemBinding.imgThumb);
            itemBinding.imgThumb.setImageResource(R.drawable.app_gradient); // Placeholder
            
            // 2. Acción al pulsar sobre la imagen (Ver en pantalla completa)
            itemBinding.getRoot().setOnClickListener(v -> {
                // Implementar visualizador de imagen
            });
            // =========================================================================
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
