package com.velvasoftware.pixelrootapp.utils;

/**
 * Los links de Dropbox que se comparten normalmente (con "?dl=0" al final) abren una
 * página HTML de vista previa, no la imagen en sí — por eso una app no puede cargarlos
 * directo como imagen. Esta clase los convierte al formato de descarga directa.
 *
 * Ejemplo:
 *   https://www.dropbox.com/s/abc123/portada.jpg?dl=0
 *   -> https://www.dropbox.com/s/abc123/portada.jpg?raw=1
 */
public class ImageUrlUtils {

    public static String toDirectImageUrl(String url) {
        if (url == null || url.isEmpty()) return url;
        if (!url.contains("dropbox.com")) return url; // otros hosts (Cloudinary, S3, etc.) se dejan tal cual

        if (url.contains("dl=0")) {
            return url.replace("dl=0", "raw=1");
        }
        if (url.contains("dl=1")) {
            return url.replace("dl=1", "raw=1");
        }
        if (url.contains("raw=1")) {
            return url; // ya viene en formato directo
        }
        // No tiene ningún parámetro de descarga: le agregamos raw=1.
        String separador = url.contains("?") ? "&" : "?";
        return url + separador + "raw=1";
    }
}