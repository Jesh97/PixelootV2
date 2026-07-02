package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.velvasoftware.pixelrootapp.databinding.FragmentTicketChatBinding;
import com.velvasoftware.pixelrootapp.databinding.ItemChatMessageBinding;
import com.velvasoftware.pixelrootapp.models.ChatMessage;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.request.ChatMessageRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.ui.common.GenericAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketChatFragment extends Fragment {

    private static final String TAG = "TicketChatFragment";
    private static final long POLL_INTERVAL_MS = 4000; // recarga cada 4 segundos

    private FragmentTicketChatBinding binding;
    private List<ChatMessage> messages = new ArrayList<>();
    private GenericAdapter<ItemChatMessageBinding, ChatMessage> adapter;
    private int ticketId = -1;

    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            loadMessages();
            pollHandler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    public TicketChatFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTicketChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ticketId = getArguments() != null ? getArguments().getInt("ticketId", -1) : -1;

        if (ticketId <= 0) {
            Toast.makeText(getContext(), "No se encontró el ticket", Toast.LENGTH_SHORT).show();
            return;
        }

        setupRecyclerView();
        setupListeners();
        loadMessages();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ticketId > 0) {
            pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void setupRecyclerView() {
        adapter = new GenericAdapter<>(messages, ItemChatMessageBinding::inflate, (itemBinding, data) -> {
            itemBinding.txtMessageBody.setText(data.getMessage());

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemBinding.containerMessage.getLayoutParams();
            if (data.isFromUser()) {
                params.gravity = Gravity.END;
                itemBinding.containerMessage.setBackgroundResource(R.drawable.bg_card_gaming);
            } else {
                params.gravity = Gravity.START;
                itemBinding.containerMessage.setBackgroundResource(R.drawable.btn_secondary_color);
            }
            itemBinding.containerMessage.setLayoutParams(params);
        });

        binding.rvChatMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvChatMessages.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSendMessage.setOnClickListener(v -> {
            String text = binding.etMessageInput.getText().toString().trim();
            if (!text.isEmpty()) {
                enviarMensaje(text);
                binding.etMessageInput.setText("");
            }
        });
    }

    private void loadMessages() {
        RetrofitClient.getTicketApi().getMensajes(ticketId).enqueue(new Callback<ApiResponse<List<ChatMessage>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<ChatMessage>>> call, @NonNull Response<ApiResponse<List<ChatMessage>>> response) {
                if (binding == null) return;

                ApiResponse<List<ChatMessage>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    int previousSize = messages.size();
                    messages.clear();
                    messages.addAll(body.getData());
                    adapter.notifyDataSetChanged();

                    // Solo hace auto-scroll si hubo mensajes nuevos, para no interrumpir
                    // al usuario si está leyendo hacia arriba en el historial.
                    if (messages.size() != previousSize && !messages.isEmpty()) {
                        binding.rvChatMessages.scrollToPosition(messages.size() - 1);
                    }
                } else {
                    Log.e(TAG, "Error al cargar mensajes: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<ChatMessage>>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo conexión mensajes", t);
                // No mostramos Toast aquí para no ser invasivos en cada intento fallido del polling.
            }
        });
    }

    private void enviarMensaje(String texto) {
        ChatMessageRequest body = new ChatMessageRequest(texto);

        RetrofitClient.getTicketApi().enviarMensaje(ticketId, body).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call, @NonNull Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    loadMessages();
                } else if (getContext() != null) {
                    Toast.makeText(getContext(), "No se pudo enviar el mensaje", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Fallo al enviar mensaje", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Sin conexión con el servidor", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pollHandler.removeCallbacks(pollRunnable);
        binding = null;
    }
}