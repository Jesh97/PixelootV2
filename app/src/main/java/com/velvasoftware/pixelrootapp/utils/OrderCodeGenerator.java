package com.velvasoftware.pixelrootapp.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Genera el código de pedido en el celular antes de confirmar la compra.
 * Formato: PXL-YYYYMMDD-XXXX (XXXX = 4 caracteres alfanuméricos al azar).
 * El backend valida que sea único antes de guardarlo (por si dos pedidos
 * generaran el mismo código en el mismo milisegundo, algo prácticamente
 * imposible pero igual cubierto server-side).
 */
public class OrderCodeGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sin 0/O/1/I para evitar confusión visual

    public static String generate() {
        String fecha = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new java.util.Date());

        Random random = new Random();
        StringBuilder sufijo = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sufijo.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return "PXL-" + fecha + "-" + sufijo;
    }
}