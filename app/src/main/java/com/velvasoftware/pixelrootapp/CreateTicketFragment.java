package com.velvasoftware.pixelrootapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.velvasoftware.pixelrootapp.databinding.FragmentCreateTicketBinding;
import com.velvasoftware.pixelrootapp.models.Branch;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.models.Ticket;
import com.velvasoftware.pixelrootapp.models.TicketPriority;
import com.velvasoftware.pixelrootapp.models.TicketType;
import com.velvasoftware.pixelrootapp.network.api.BranchApi;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.TicketApi;
import com.velvasoftware.pixelrootapp.network.request.CreateTicketRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;
import com.velvasoftware.pixelrootapp.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.Editable;
import android.text.TextWatcher;
import com.velvasoftware.pixelrootapp.network.SessionManager;

public class CreateTicketFragment extends Fragment {

    private FragmentCreateTicketBinding binding;

    private final List<TicketType> ticketTypes = new ArrayList<>();
    private final List<TicketPriority> priorities = new ArrayList<>();
    private final List<Order> userOrders = new ArrayList<>();
    private final List<Branch> branches = new ArrayList<>();
    private TicketType selectedType;
    private Order selectedOrder;
    private Integer selectedBranchId = null;

    private static final String TIPO_REEMBOLSO = "REEMBOLSO";

    public CreateTicketFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCreateTicketBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupQrScanner();
        setupFormValidation();
        setupListeners();
        loadTicketOptions();
        loadUserOrders();
    }

    private void loadUserOrders() {
        OrderApi api = RetrofitClient.getOrderApi();
        api.getOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Response<ApiResponse<List<Order>>> response) {
                if (binding == null) return;

                ApiResponse<List<Order>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    userOrders.clear();
                    userOrders.addAll(body.getData());
                    setupOrderSelector();
                } else {
                    Log.e("TICKETS_API", "No se pudieron cargar pedidos: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar tus pedidos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Order>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("TICKETS_API", "Fallo al cargar pedidos", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupOrderSelector() {
        // Ya no se arma ningún dropdown aquí: este campo es de solo lectura y únicamente
        // se llena cuando se escanea el QR (ver setupQrScanner). userOrders queda en memoria
        // solo para validar que el código escaneado corresponda a un pedido real del usuario.
        if (userOrders.isEmpty()) {
            binding.etRelatedOrder.setHint("No tienes pedidos registrados");
        }
    }

    private void loadTicketOptions() {
        TicketApi api = RetrofitClient.getTicketApi();

        api.getTicketTypes().enqueue(new Callback<ApiResponse<List<TicketType>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TicketType>>> call, @NonNull Response<ApiResponse<List<TicketType>>> response) {
                if (binding == null) return;

                ApiResponse<List<TicketType>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    ticketTypes.clear();
                    ticketTypes.addAll(body.getData());
                    setupTypeSelector();
                } else {
                    Log.e("TICKETS_API", "No se pudieron cargar tipos: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar los tipos de incidencia", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TicketType>>> call, @NonNull Throwable t) {
                if (binding == null) return;
                Log.e("TICKETS_API", "Fallo al cargar tipos", t);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        api.getTicketPriorities().enqueue(new Callback<ApiResponse<List<TicketPriority>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<TicketPriority>>> call, @NonNull Response<ApiResponse<List<TicketPriority>>> response) {
                if (binding == null) return;

                ApiResponse<List<TicketPriority>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    priorities.clear();
                    priorities.addAll(body.getData());
                } else {
                    Log.e("TICKETS_API", "No se pudieron cargar prioridades: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<TicketPriority>>> call, @NonNull Throwable t) {
                Log.e("TICKETS_API", "Fallo al cargar prioridades", t);
            }
        });
    }

    private void setupTypeSelector() {
        List<String> names = new ArrayList<>();
        for (TicketType type : ticketTypes) {
            names.add(formatTypeName(type.getName()));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdown_option, names);
        binding.autoCompleteType.setAdapter(adapter);

        binding.autoCompleteType.setOnItemClickListener((parent, v, position, id) -> {
            selectedType = ticketTypes.get(position);
            onTicketTypeSelected();
            validateForm();
        });
    }

    /**
     * El motivo "REEMBOLSO" es especial: además del pedido y la descripción, requiere que
     * el usuario elija en qué sucursal hará la devolución física del producto.
     */
    private void onTicketTypeSelected() {
        boolean esReembolso = selectedType != null
                && TIPO_REEMBOLSO.equalsIgnoreCase(selectedType.getName());

        binding.sectionSucursalDevolucion.setVisibility(esReembolso ? View.VISIBLE : View.GONE);

        if (esReembolso && branches.isEmpty()) {
            loadBranches();
        }
        if (!esReembolso) {
            selectedBranchId = null;
        }
    }

    private void loadBranches() {
        BranchApi api = RetrofitClient.getBranchApi();
        api.getBranches().enqueue(new Callback<ApiResponse<List<Branch>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Response<ApiResponse<List<Branch>>> response) {
                if (binding == null) return;

                ApiResponse<List<Branch>> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    branches.clear();
                    branches.addAll(body.getData());
                    bindSucursalChips();
                } else {
                    Log.e("TICKETS_API", "No se pudieron cargar sucursales: " + response.code());
                    Toast.makeText(getContext(), "No se pudieron cargar las sucursales", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Branch>>> call, @NonNull Throwable t) {
                Log.e("TICKETS_API", "Fallo al cargar sucursales", t);
                if (binding == null) return;
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindSucursalChips() {
        binding.cgSucursalesDevolucion.removeAllViews();
        selectedBranchId = null;

        for (Branch branch : branches) {
            com.google.android.material.chip.Chip chip =
                    new com.google.android.material.chip.Chip(requireContext(), null, com.google.android.material.R.attr.chipStyle);
            chip.setText(branch.getName());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setTag(branch.getId());
            styleSucursalChip(chip);

            chip.setOnClickListener(v -> {
                for (int i = 0; i < binding.cgSucursalesDevolucion.getChildCount(); i++) {
                    com.google.android.material.chip.Chip other =
                            (com.google.android.material.chip.Chip) binding.cgSucursalesDevolucion.getChildAt(i);
                    other.setChecked(other == chip);
                    styleSucursalChip(other);
                }
                selectedBranchId = (int) chip.getTag();
                validateForm();
            });

            binding.cgSucursalesDevolucion.addView(chip);
        }
    }

    private void styleSucursalChip(com.google.android.material.chip.Chip chip) {
        boolean checked = chip.isChecked();
        chip.setChipBackgroundColorResource(checked ? R.color.verde_claro_pixel : R.color.negro_oscuro);
        chip.setTextColor(getResources().getColor(checked ? R.color.negro_oscuro : R.color.blanco_intermedio));
        chip.setChipStrokeColorResource(checked ? R.color.verde_claro_pixel : R.color.verde_oscuro_pixel);
    }

    /** "JUEGO_NO_DESCARGA" -> "Juego no descarga" (más legible para el usuario). */
    private String formatTypeName(String raw) {
        if (raw == null) return "";
        String withSpaces = raw.replace("_", " ").toLowerCase();
        return withSpaces.substring(0, 1).toUpperCase() + withSpaces.substring(1);
    }

    private void setupListeners() {
        binding.btnSubmitTicket.setOnClickListener(v -> submitTicket());
    }

    private void submitTicket() {
        if (selectedType == null) {
            Toast.makeText(getContext(), "Selecciona el tipo de incidencia", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedOrder == null) {
            Toast.makeText(getContext(), "Selecciona el pedido relacionado", Toast.LENGTH_SHORT).show();
            return;
        }
        int pedidoId = selectedOrder.getOrderId();

        String description = binding.etDescription.getText() != null ? binding.etDescription.getText().toString().trim() : "";
        if (description.length() <= 5) {
            Toast.makeText(getContext(), "Describe el problema con más detalle", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean esReembolso = TIPO_REEMBOLSO.equalsIgnoreCase(selectedType.getName());
        if (esReembolso && selectedBranchId == null) {
            Toast.makeText(getContext(), "Selecciona la sucursal de devolución", Toast.LENGTH_SHORT).show();
            return;
        }

        // El formulario no le pide prioridad al usuario; usamos "MEDIA" por defecto si existe
        // en las prioridades reales que trajo la API (si no, la primera disponible).
        int prioridadId = findPriorityIdByName("MEDIA");

        binding.btnSubmitTicket.setEnabled(false);

        CreateTicketRequest request = new CreateTicketRequest(
                pedidoId,
                selectedType.getId(),
                prioridadId,
                formatTypeName(selectedType.getName()), // el formulario no tiene campo "título" propio, usamos el tipo elegido
                description,
                esReembolso ? selectedBranchId : null // sucursal_id: solo aplica para devoluciones/reembolsos
        );

        TicketApi api = RetrofitClient.getTicketApi();
        api.createTicket(request).enqueue(new Callback<ApiResponse<Ticket>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Ticket>> call, @NonNull Response<ApiResponse<Ticket>> response) {
                if (binding == null) return;
                binding.btnSubmitTicket.setEnabled(true);

                ApiResponse<Ticket> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus()) {
                    Toast.makeText(getContext(), "Ticket creado correctamente", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(binding.getRoot()).navigateUp();
                } else {
                    String message = body != null ? body.getMessage() : "No se pudo crear el ticket";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Ticket>> call, @NonNull Throwable t) {
                if (binding == null) return;
                binding.btnSubmitTicket.setEnabled(true);
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private int findPriorityIdByName(String name) {
        for (TicketPriority p : priorities) {
            if (name.equalsIgnoreCase(p.getName())) return p.getId();
        }
        return priorities.isEmpty() ? 1 : priorities.get(0).getId();
    }

    private void setupQrScanner() {
        binding.btnScanQr.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("mode_return_result", true);
            Navigation.findNavController(v).navigate(R.id.qrScannerFragment, args);
        });

        // Escuchar el resultado del escáner QR
        getParentFragmentManager().setFragmentResultListener("qr_scan_request", getViewLifecycleOwner(), (requestKey, result) -> {
            String scannedValue = result.getString("scanned_order_id");
            if (scannedValue == null) return;

            // Primero intentamos buscar coincidencia por el nuevo codigo_pedido (hexadecimal)
            Order match = null;
            for (Order order : userOrders) {
                if (scannedValue.equals(order.getOrderCode())) {
                    match = order;
                    break;
                }
            }

            // Si no hay coincidencia, intentamos por ID numérico (compatibilidad)
            if (match == null) {
                String digits = scannedValue.replaceAll("[^0-9]", "");
                for (Order order : userOrders) {
                    if (String.valueOf(order.getOrderId()).equals(digits)) {
                        match = order;
                        break;
                    }
                }
            }

            if (match != null) {
                selectedOrder = match;
                binding.etRelatedOrder.setText("Pedido #" + match.getOrderId() + " (" + match.getDate() + ")");
                validateForm();
            } else {
                // Si el usuario es operativo (Agente, Admin, Superadmin), permitimos cualquier código
                int roleId = SessionManager.getInstance(requireContext()).getRolId();
                if (roleId >= 2) {
                    binding.etRelatedOrder.setText(scannedValue);
                    validateForm();
                } else {
                    Toast.makeText(getContext(), "Ese código no corresponde a ninguno de tus pedidos", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setupFormValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        binding.etRelatedOrder.addTextChangedListener(watcher);
        binding.etDescription.addTextChangedListener(watcher);
    }

    private void validateForm() {
        boolean isOrderValid = (selectedOrder != null) || (binding.etRelatedOrder.getText() != null && binding.etRelatedOrder.getText().length() > 0);
        boolean isDescValid = binding.etDescription.getText() != null && binding.etDescription.getText().length() > 5;

        boolean esReembolso = selectedType != null && TIPO_REEMBOLSO.equalsIgnoreCase(selectedType.getName());
        boolean isBranchValid = !esReembolso || selectedBranchId != null;

        binding.btnSubmitTicket.setEnabled(isOrderValid && isDescValid && isBranchValid);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}