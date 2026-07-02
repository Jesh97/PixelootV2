package com.velvasoftware.pixelrootapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmationFragment extends Fragment {

    private FragmentOrderConfirmationBinding binding;
    private Order currentOrder;

    public OrderConfirmationFragment() {}

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
                generateOrderPdf(currentOrder);
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

    private void generateOrderPdf(Order order) {
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
        if (order.getOrderCode() != null && !order.getOrderCode().isEmpty()) {
            canvas.drawText("Código: " + order.getOrderCode(), 20, y, paint);
            y += 25;
        }

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
            String contenidoQr = (order.getOrderCode() != null && !order.getOrderCode().isEmpty())
                    ? order.getOrderCode()
                    : "PEDIDO-" + order.getOrderId(); // respaldo por si el pedido es viejo y no tiene código
            Bitmap qrBitmap = generateQrCode(contenidoQr);
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

        File filePath = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Recibo_" + order.getOrderId() + ".pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(getContext(), "Recibo guardado en: " + filePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
        document.close();
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