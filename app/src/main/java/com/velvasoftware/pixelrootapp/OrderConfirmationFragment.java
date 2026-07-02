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
import com.velvasoftware.pixelrootapp.utils.CurrencyUtils;

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
        // Paleta exacta de la app (colors.xml)
        final int negroOscuro = Color.parseColor("#0B0E0C");
        final int negroIntermedio = Color.parseColor("#0E160F");
        final int blancoClaro = Color.parseColor("#ECF9F2");
        final int blancoIntermedio = Color.parseColor("#D9F2E5");
        final int verdeClaro = Color.parseColor("#11ED5A");
        final int verdeOscuro = Color.parseColor("#015D33");
        final int amarilloClaro = Color.parseColor("#A5E51A");
        final int rojoError = Color.parseColor("#FF4C4C");

        int pageWidth = 380;
        int margin = 30;
        int itemCount = order.getItems() != null ? order.getItems().size() : 0;
        int qrSize = 150;

        // Calculamos el alto exacto que va a ocupar todo el contenido para que NADA se
        // superponga ni se corte, sin importar cuántos productos tenga el pedido.
        int pageHeight = 110                 // header
                + 70                          // caja de código de pedido
                + 40                          // fila pedido/estado
                + 40                          // título "PRODUCTOS" + línea
                + (itemCount * 28)             // filas de productos
                + 20
                + 3 * 34                       // subtotal + igv + total
                + 30                           // separación antes del QR
                + qrSize + 24 + 40             // marco QR + caption
                + 70;                          // footer

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Fondo general
        canvas.drawColor(negroOscuro);

        // ---------- Header ----------
        Paint headerBg = new Paint();
        headerBg.setColor(negroIntermedio);
        canvas.drawRect(0, 0, pageWidth, 92, headerBg);

        paint.setColor(verdeClaro);
        paint.setTextSize(26f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("pixel root", pageWidth / 2f, 46, paint);

        paint.setColor(blancoIntermedio);
        paint.setTextSize(11f);
        paint.setFakeBoldText(false);
        canvas.drawText("RECIBO DE COMPRA", pageWidth / 2f, 68, paint);

        paint.setTextAlign(Paint.Align.LEFT);

        Paint divider = new Paint();
        divider.setColor(verdeOscuro);
        divider.setStrokeWidth(2f);
        canvas.drawLine(0, 92, pageWidth, 92, divider);

        int y = 135;

        // ---------- Bloque de código de pedido ----------
        String codigo = (order.getOrderCode() != null && !order.getOrderCode().isEmpty())
                ? order.getOrderCode() : "PEDIDO-" + order.getOrderId();

        int boxTop = y - 20;
        int boxBottom = y + 26;

        Paint codeBox = new Paint();
        codeBox.setColor(negroIntermedio);
        canvas.drawRoundRect(margin, boxTop, pageWidth - margin, boxBottom, 10, 10, codeBox);
        Paint codeBoxStroke = new Paint();
        codeBoxStroke.setStyle(Paint.Style.STROKE);
        codeBoxStroke.setColor(verdeOscuro);
        codeBoxStroke.setStrokeWidth(1.5f);
        canvas.drawRoundRect(margin, boxTop, pageWidth - margin, boxBottom, 10, 10, codeBoxStroke);

        paint.setColor(blancoIntermedio);
        paint.setTextSize(9f);
        canvas.drawText("CÓDIGO DE PEDIDO", margin + 14, y - 3, paint);
        paint.setColor(verdeClaro);
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        canvas.drawText(codigo, margin + 14, y + 18, paint);

        y = boxBottom + 34;

        // Estado con color según valor
        int colorEstado;
        String estado = order.getStatus() != null ? order.getStatus() : "PENDIENTE";
        switch (estado) {
            case "COMPLETADO": colorEstado = verdeClaro; break;
            case "CANCELADO": colorEstado = rojoError; break;
            default: colorEstado = amarilloClaro; break;
        }

        paint.setColor(blancoIntermedio);
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText("Pedido #" + order.getOrderId(), margin, y, paint);

        paint.setColor(colorEstado);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(estado, pageWidth - margin, y, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        y += 36;

        // ---------- Productos ----------
        paint.setColor(verdeClaro);
        paint.setTextSize(11f);
        paint.setFakeBoldText(true);
        canvas.drawText("PRODUCTOS", margin, y, paint);
        y += 10;
        canvas.drawLine(margin, y, pageWidth - margin, y, divider);
        y += 28;

        paint.setTextSize(12f);
        if (order.getItems() != null) {
            boolean alterna = false;
            for (CartItem item : order.getItems()) {
                if (alterna) {
                    Paint rowBg = new Paint();
                    rowBg.setColor(negroIntermedio);
                    canvas.drawRect(margin - 8, y - 16, pageWidth - margin + 8, y + 10, rowBg);
                }
                alterna = !alterna;

                paint.setColor(blancoClaro);
                paint.setFakeBoldText(false);
                canvas.drawText(item.getQuantity() + "x " + item.getTitle(), margin, y, paint);

                paint.setColor(blancoIntermedio);
                paint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(CurrencyUtils.format(item.getSubtotal()), pageWidth - margin, y, paint);
                paint.setTextAlign(Paint.Align.LEFT);

                y += 28;
            }
        }

        y += 6;
        canvas.drawLine(margin, y, pageWidth - margin, y, divider);
        y += 30;

        // ---------- Totales ----------
        y = drawTotalRow(canvas, paint, "Subtotal", CurrencyUtils.format(order.getSubtotal()), margin, pageWidth - margin, y, blancoIntermedio, 12f, false);
        y = drawTotalRow(canvas, paint, "IGV (18%)", CurrencyUtils.format(order.getTax()), margin, pageWidth - margin, y, blancoIntermedio, 12f, false);
        y += 6;
        canvas.drawLine(margin, y, pageWidth - margin, y, divider);
        y += 30;
        y = drawTotalRow(canvas, paint, "TOTAL", CurrencyUtils.format(order.getTotal()), margin, pageWidth - margin, y, verdeClaro, 18f, true);

        y += 36;

        // ---------- QR ----------
        try {
            Bitmap qrBitmap = generateQrCode(codigo);
            if (qrBitmap != null) {
                int qrX = (pageWidth - qrSize) / 2;

                Paint qrFrame = new Paint();
                qrFrame.setAntiAlias(true);
                qrFrame.setColor(Color.WHITE);
                canvas.drawRect(qrX - 14, y - 14, qrX + qrSize + 14, y + qrSize + 14, qrFrame);

                Bitmap qrEscalado = Bitmap.createScaledBitmap(qrBitmap, qrSize, qrSize, true);
                canvas.drawBitmap(qrEscalado, qrX, y, paint);

                y += qrSize + 30;
                paint.setColor(blancoIntermedio);
                paint.setTextSize(10f);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("Escanea este código para verificar tu pedido", pageWidth / 2f, y, paint);
                paint.setTextAlign(Paint.Align.LEFT);
                y += 40;
            }
        } catch (WriterException e) {
            e.printStackTrace();
            y += qrSize + 70;
        }

        // ---------- Footer ----------
        paint.setColor(verdeOscuro);
        paint.setTextSize(10f);
        paint.setFakeBoldText(false);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("¡Gracias por tu compra en Pixel Root!", pageWidth / 2f, pageHeight - 34, paint);
        paint.setTextSize(8f);
        canvas.drawText("© 2026 Velva Software · Perú", pageWidth / 2f, pageHeight - 20, paint);
        paint.setTextAlign(Paint.Align.LEFT);

        document.finishPage(page);

        File filePath = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Recibo_" + codigo + ".pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
            openOrShareFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    /** Fila "etiqueta ......... valor" alineada a los extremos, usada para subtotal/IGV/total. */
    private int drawTotalRow(Canvas canvas, Paint paint, String label, String value, int left, int right, int y,
                             int color, float textSize, boolean bold) {
        paint.setColor(color);
        paint.setTextSize(textSize);
        paint.setFakeBoldText(bold);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, left, y, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(value, right, y, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setFakeBoldText(false);
        return y + (int) (textSize + 14);
    }

    /** Abre el selector del sistema para ver/guardar/compartir el PDF recién generado. */
    private void openOrShareFile(File file) {
        android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(), requireContext().getPackageName() + ".fileprovider", file);

        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(android.content.Intent.createChooser(intent, "Abrir o guardar recibo"));
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Recibo guardado en: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
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