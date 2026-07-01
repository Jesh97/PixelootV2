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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OrderConfirmationFragment extends Fragment {

    private FragmentOrderConfirmationBinding binding;

    public OrderConfirmationFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupListeners();
    }

    private void setupListeners() {
        binding.btnDownloadReceipt.setOnClickListener(v -> generateOrderPdf("PR-85920", "Neon Abyss II", 59.99));
        
        binding.btnBackHome.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.homeFragment)
        );
    }

    private void generateOrderPdf(String orderId, String gameTitle, double price) {
        // =========================================================================
        // GENERACIÓN DE RECIBO PDF CON DISEÑO Y CÓDIGO QR
        // =========================================================================
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Estilo de la App (Fondo oscuro)
        canvas.drawColor(Color.parseColor("#0F0F0F"));
        
        // Título de la tienda
        paint.setColor(Color.parseColor("#39FF14")); // Verde Pixel
        paint.setTextSize(22f);
        paint.setFakeBoldText(true);
        canvas.drawText("PIXEL ROOT STORE", 50, 60, paint);

        // Línea divisoria
        paint.setStrokeWidth(2f);
        canvas.drawLine(20, 80, 280, 80, paint);

        // Datos del Pedido
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(false);
        paint.setTextSize(14f);
        canvas.drawText("ID de Pedido: " + orderId, 20, 120, paint);
        canvas.drawText("Producto: " + gameTitle, 20, 150, paint);
        canvas.drawText("Precio Total: $" + price, 20, 180, paint);
        canvas.drawText("Estado: Confirmado", 20, 210, paint);

        // GENERACIÓN DEL QR DINÁMICO
        try {
            Bitmap qrBitmap = generateQrCode(orderId);
            if (qrBitmap != null) {
                // Centrar el QR en el PDF
                canvas.drawBitmap(qrBitmap, 50, 280, paint);
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }

        paint.setColor(Color.GRAY);
        paint.setTextSize(10f);
        canvas.drawText("Escanee el código para ver el estado en la App", 40, 520, paint);

        document.finishPage(page);

        // Guardar el archivo en el almacenamiento del dispositivo
        File filePath = new File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Recibo_" + orderId + ".pdf");
        try {
            document.writeTo(new FileOutputStream(filePath));
            Toast.makeText(getContext(), "Recibo guardado en: " + filePath.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error al guardar el archivo", Toast.LENGTH_SHORT).show();
        }
        document.close();
        // =========================================================================
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
