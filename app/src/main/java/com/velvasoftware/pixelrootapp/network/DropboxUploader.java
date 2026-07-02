package com.velvasoftware.pixelrootapp.network;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DropboxUploader {

    // ⚠️ Solo para demo académica. En producción real esto NO debe ir hardcodeado.
    private static final String APP_KEY = "i9vag11wlgwmdd0";
    private static final String APP_SECRET = "fw70qe2gwwgudni";
    private static final String REFRESH_TOKEN = "f_5PjprGzvOAAAAAAAAATCL_RZ4djxlqteKzh7axTyt-0rpt1ECTgRlguBA8jNL";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface UploadCallback {
        void onSuccess(String sharedUrl);
        void onError(String message);
    }

    /** Pide un access_token nuevo usando el refresh_token (este último nunca expira). */
    private static String getFreshAccessToken() throws IOException {
        RequestBody body = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", REFRESH_TOKEN)
                .add("client_id", APP_KEY)
                .add("client_secret", APP_SECRET)
                .build();

        Request request = new Request.Builder()
                .url("https://api.dropboxapi.com/oauth2/token")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseText = response.body() != null ? response.body().string() : "(sin cuerpo)";

            android.util.Log.e("DropboxUploader", "Respuesta refresh (" + response.code() + "): " + responseText);

            if (!response.isSuccessful()) {
                throw new IOException("No se pudo renovar el token: " + response.code() + " - " + responseText);
            }

            try {
                return new JSONObject(responseText).getString("access_token");
            } catch (Exception e) {
                throw new IOException("Respuesta inesperada al renovar token: " + responseText);
            }
        }
    }

    /** Sube un archivo desde una Uri local (galería) a Dropbox y devuelve el link compartido. */
    public static void uploadFile(Context context, Uri fileUri, UploadCallback callback) {
        new Thread(() -> {
            try {
                // 0. Renovar el access_token antes de cualquier operación
                String accessToken = getFreshAccessToken();

                InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
                if (inputStream == null) {
                    callback.onError("No se pudo leer el archivo");
                    return;
                }

                byte[] fileBytes = readAllBytes(inputStream);
                inputStream.close();

                String fileName = "ticket_" + UUID.randomUUID().toString() + ".jpg";
                String dropboxPath = "/tickets/" + fileName;

                // 1. Subir el archivo
                JSONObject dropboxArg = new JSONObject();
                dropboxArg.put("path", dropboxPath);
                dropboxArg.put("mode", "add");
                dropboxArg.put("autorename", true);

                RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), fileBytes);

                Request uploadRequest = new Request.Builder()
                        .url("https://content.dropboxapi.com/2/files/upload")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Dropbox-API-Arg", dropboxArg.toString())
                        .addHeader("Content-Type", "application/octet-stream")
                        .post(body)
                        .build();

                try (Response uploadResponse = client.newCall(uploadRequest).execute()) {
                    if (!uploadResponse.isSuccessful()) {
                        callback.onError("Error al subir: " + uploadResponse.code());
                        return;
                    }
                }

                // 2. Crear link compartido
                JSONObject sharedArg = new JSONObject();
                sharedArg.put("path", dropboxPath);

                Request shareRequest = new Request.Builder()
                        .url("https://api.dropboxapi.com/2/sharing/create_shared_link_with_settings")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(MediaType.parse("application/json"), sharedArg.toString()))
                        .build();

                try (Response shareResponse = client.newCall(shareRequest).execute()) {
                    if (!shareResponse.isSuccessful()) {
                        callback.onError("Error al crear link: " + shareResponse.code());
                        return;
                    }
                    String responseBody = shareResponse.body().string();
                    JSONObject json = new JSONObject(responseBody);
                    String rawLink = json.getString("url");

                    // Convierte el link de vista previa en uno de descarga directa
                    String directLink = rawLink.replace("?dl=0", "?raw=1");

                    callback.onSuccess(directLink);
                }

            } catch (Exception e) {
                callback.onError(e.getMessage());
            }
        }).start();
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}