package com.velvasoftware.pixelrootapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.velvasoftware.pixelrootapp.databinding.FragmentOrderConfirmationBinding;
import com.velvasoftware.pixelrootapp.models.CartItem;
import com.velvasoftware.pixelrootapp.models.Order;
import com.velvasoftware.pixelrootapp.network.api.OrderApi;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmationFragment extends Fragment {

    private FragmentOrderConfirmationBinding binding;
    private Order currentOrder;

    // Guardamos el PDF ya generado en memoria hasta que el usuario elija dónde guardarlo
    private byte[] pendingPdfBytes;
    private String pendingFileName;

    // Selector de ubicación (Android 10+, no requiere permiso de almacenamiento)
    private ActivityResultLauncher<String> createDocumentLauncher;

    // Permiso clásico, solo necesario en Android 9 o menor (API < 29)
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public OrderConfirmationFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Debe registrarse en onCreate (antes de que la vista exista), no en onViewCreated.
        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/pdf"),
                uri -> {
                    if (uri != null && pendingPdfBytes != null) {
                        writePdfToUri(uri);
                    }
                }
        );

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        launchDocumentPicker();
                    } else {
                        Toast.makeText(getContext(), "Se necesita el permiso para guardar el recibo", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int orderId = getArguments() != null ? getArguments().getInt("orderId", -1) : -1;
        double total = getArguments() != null ? getArguments().getDouble("total", 0) : 0;

        binding.txtOrderIdConfirmation.setText("Pedido #" + orderId + " — " + com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(total));

        setupListeners(orderId);
        loadOrderDetail(orderId);
    }

    private void setupListeners(int orderId) {
        binding.btnDownloadReceipt.setOnClickListener(v -> {
            if (currentOrder != null) {
                onDownloadRequested(currentOrder);
            } else {
                Toast.makeText(getContext(), "Espera un momento, cargando el pedido...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnBackHome.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.homeFragment)
        );
    }

    private void loadOrderDetail(int orderId) {
        if (orderId <= 0) return;

        OrderApi api = RetrofitClient.getOrderApi();
        api.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Order>> call, @NonNull Response<ApiResponse<Order>> response) {
                ApiResponse<Order> body = response.body();
                if (response.isSuccessful() && body != null && body.isStatus() && body.getData() != null) {
                    currentOrder = body.getData();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Order>> call, @NonNull Throwable t) {
                // El usuario igual puede volver a Home; el recibo simplemente no se podrá generar
                // hasta que haya conexión.
            }
        });
    }

    // =========================================================================
    // PASO 1: generar el PDF en memoria, luego decidir si pedir permiso o abrir el selector directo
    // =========================================================================
    private void onDownloadRequested(Order order) {
        pendingFileName = "Recibo_" + order.getOrderId() + ".pdf";
        pendingPdfBytes = buildOrderPdfBytes(order);

        if (pendingPdfBytes == null) {
            Toast.makeText(getContext(), "No se pudo generar el recibo", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+: el selector de archivos no necesita permiso de almacenamiento.
            launchDocumentPicker();
        } else {
            // Android 9 o menor: sí se necesita el permiso clásico antes de escribir en disco.
            boolean granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                launchDocumentPicker();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    // =========================================================================
    // PASO 2: abrir el selector nativo de "Guardar como..." para que el usuario elija ubicación
    // =========================================================================
    private void launchDocumentPicker() {
        createDocumentLauncher.launch(pendingFileName);
    }

    // =========================================================================
    // PASO 3: el usuario ya eligió dónde guardar (uri), escribimos el PDF ahí
    // =========================================================================
    private void writePdfToUri(Uri uri) {
        try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
            if (out != null) {
                out.write(pendingPdfBytes);
                Toast.makeText(getContext(), "Recibo guardado correctamente", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        } finally {
            pendingPdfBytes = null;
        }
    }

    // =========================================================================
    // Generación del PDF (misma lógica de antes, pero devuelve bytes en vez de escribir a disco)
    // =========================================================================
    private byte[] buildOrderPdfBytes(Order order) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        canvas.drawColor(Color.parseColor("#0F0F0F"));

        paint.setColor(Color.parseColor("#39FF14"));
        paint.setTextSize(22f);
        paint.setFakeBoldText(true);
        canvas.drawText("PIXEL ROOT STORE", 50, 60, paint);

        paint.setStrokeWidth(2f);
        canvas.drawLine(20, 80, 280, 80, paint);

        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(false);
        paint.setTextSize(14f);

        int y = 120;
        canvas.drawText("ID de Pedido: #" + order.getOrderId(), 20, y, paint);
        y += 25;

        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                String linea = item.getQuantity() + "x " + item.getTitle();
                canvas.drawText(linea, 20, y, paint);
                y += 20;
            }
        }

        y += 10;
        canvas.drawText("Subtotal: " + com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(order.getSubtotal()), 20, y, paint);
        y += 20;
        canvas.drawText("IGV: " + com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(order.getTax()), 20, y, paint);
        y += 20;
        paint.setColor(Color.parseColor("#39FF14"));
        paint.setFakeBoldText(true);
        canvas.drawText("Total: " + com.velvasoftware.pixelrootapp.utils.CurrencyUtils.format(order.getTotal()), 20, y, paint);
        y += 30;

        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(false);
        canvas.drawText("Estado: " + order.getStatus(), 20, y, paint);

        try {
            Bitmap qrBitmap = generateQrCode("PEDIDO-" + order.getOrderId());
            if (qrBitmap != null) {
                canvas.drawBitmap(qrBitmap, 50, 300, paint);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }

        paint.setColor(Color.GRAY);
        paint.setTextSize(10f);
        canvas.drawText("Escanee el código para ver el estado en la App", 40, 520, paint);

        document.finishPage(page);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            document.writeTo(baos);
        } catch (IOException e) {
            e.printStackTrace();
            document.close();
            return null;
        }
        document.close();
        return baos.toByteArray();
    }

    private Bitmap generateQrCode(String text) throws WriterException {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        return barcodeEncoder.createBitmap(bitMatrix);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}