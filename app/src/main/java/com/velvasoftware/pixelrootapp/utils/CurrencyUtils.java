package com.velvasoftware.pixelrootapp.utils;

import java.util.Locale;

public class CurrencyUtils {

    public static String format(double amount) {
        return String.format(Locale.forLanguageTag("es-PE"), "S/ %.2f", amount);
    }
}