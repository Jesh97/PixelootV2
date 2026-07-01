package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.velvasoftware.pixelrootapp.databinding.FragmentTicketDetailBinding;

public class TicketDetailFragment extends Fragment {

    private FragmentTicketDetailBinding binding;

    public TicketDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.btnViewChat.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.ticketChatFragment)
        );

        binding.btnViewTimeline.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.ticketTimelineFragment)
        );

        binding.btnViewAttachments.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.ticketAttachmentsFragment)
        );

        binding.btnReopenTicket.setOnClickListener(v -> {
            // Lógica para reabrir ticket
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}