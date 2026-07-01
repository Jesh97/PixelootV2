package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.velvasoftware.pixelrootapp.databinding.FragmentTicketChatBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemChatMessageBinding;
import com.velvasoftware.pixelrootapp.models.ChatMessage;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

public class TicketChatFragment extends Fragment {

    private FragmentTicketChatBinding binding;
    private DatabaseReference chatRef;
    private List<ChatMessage> messages = new ArrayList<>();
    private GenericAdapter<ItemChatMessageBinding, ChatMessage> adapter;
    private String ticketId = "TK-9283"; // Ejemplo de ID de ticket

    public TicketChatFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupChatApi();
        setupListeners();
    }

    private void setupChatApi() {
        // =========================================================================
        // CONFIGURACIÓN DE CHAT EN TIEMPO REAL (FIREBASE REALTIME DB)
        // =========================================================================
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(ticketId);
        
        adapter = new GenericAdapter<>(messages, ItemChatMessageBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtMessageBody.setText(data.getMessage());
            
            // Lógica para alinear el mensaje según el emisor
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemBinding.containerMessage.getLayoutParams();
            if (data.isFromUser()) {
                params.gravity = Gravity.END;
                itemBinding.containerMessage.setBackgroundResource(R.drawable.bg_card_gaming); // Color usuario
            } else {
                params.gravity = Gravity.START;
                itemBinding.containerMessage.setBackgroundResource(R.drawable.btn_secondary_color); // Color soporte
            }
            itemBinding.containerMessage.setLayoutParams(params);
        });

        binding.rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChatMessages.setAdapter(adapter);

        // Escuchar nuevos mensajes en tiempo real
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ChatMessage msg = ds.getValue(ChatMessage.class);
                    if (msg != null) {
                        messages.add(msg);
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    binding.rvChatMessages.smoothScrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Manejar error de lectura
            }
        });
        // =========================================================================
    }

    private void setupListeners() {
        binding.btnSendMessage.setOnClickListener(v -> {
            String text = binding.etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessageToFirebase(text);
                binding.etMessageInput.setText("");
            }
        });
    }

    private void sendMessageToFirebase(String text) {
        // En un caso real, el senderId vendría de la sesión del usuario (Auth)
        ChatMessage msg = new ChatMessage("user_123", text, System.currentTimeMillis(), true);
        chatRef.push().setValue(msg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
