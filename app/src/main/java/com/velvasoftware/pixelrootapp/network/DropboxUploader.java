package com.velvasoftware.pixelrootapp.network;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DropboxUploader {


    private static final String ACCESS_TOKEN = "sl.u.AGk4SPfkm5qZUqZQdjHyXX66z0SDcqhttAoDIRoXRwIihs4zRhoW-_a80bk4oqIiinhNYDU2kw3TF9mnQzsAV8OLzJv82UheFio0Xb4bpvhSV07DxGJ9e8Rh-wsELX4ufbbkPSEeB4uua6EYmMvOBfRUnkhRsjdDpIyluyKv2hD1rmv_frjnbKaL0C9Ba500oS5KYqtR6UDVB6PcMd0zRTv_hm_yBLlp-rR_APK3IA6chKkebrhjlBvAIiV9ZmuMdzFi6NJwaJ9yVOPyh1eVCtuKdCSjWqosYKxzKpBbAVBtdj4M1rsVpFMBtmEEGEckIqnH9sGM3NxVsYtBVUr7yzWnQxAUag8QUaVNSHLB6rmkaIjNynGVJlwSib8H3SZAFuX1EiSMeB5DPOLWE-SAv5G16fHV2OUTPz96hCzvOtOSk20ZcAbz_AwTvn2F1K8RZIdD3kaTNyQUfNy9yJMS9WRHie40y1zxUQxCTZmrUoK6ygxETZ-qtDMnffKfHNZOu_miuaWrdPcnwcSOwYuZyd22ajxaB5DZxVHVyw5hThq6NxlCDpah0T6fyQGv0qBDCj3htq4EQIx2LnEgrcQQRe1t5Te9eGJvZxQf10ho5jyO-ydYz1155iRJ2cOPDtfdmtWaqiyBEgzzXHUXviPBcKJBMwXRwmKCR8qUtvQy2Zck1ts3UbcWZvzUAGg04XPA66GY3ewKcAQleoBwkX6auz7W3xj6KG-hkM2rkU0y-1csSfPJ2QFO809E9GAyuhIvcfgvRav4Xbw1ASnb-OwTUaSP4NAcaQK925dev-gpzFANsXF4YBg2mLGzmNEjsf5_Wjx_34-ZhWWSqSb2a8GKohzMzUx7DVseveSFidqcm_ws9qXG6L2QO_lgXdoOt3fFafeF99EFXYdZomZZ9jELoofXe3AAnEk6k7tE978BzZhA_-aVcoGWFGlz-YllCnk4cNaZbvuGOMDZ7CVFF8IU2PLgvJ3Vqe2jY4CQRMsPXMTtSkJjR7wVFo_RFAYLuO1bWE2YSyaNqCvJK3lkH5otVPiROFrNA6-FcdqkyTRkGa_AQYuyByl5HzV4msM-m5TPLHvvbXfb_WRdpLu60b-lR92YAwFjt0Eo8CUSexRuqCDFsYQvyFHgBso5gEgW9CWQJOhM7n9s67veRNZ4mC8Ii9ogM1zFxlDe9YWeG87CE0Fk7gSK6igMMNsEayBhDsblBEJh29A0OlG0H3p_hNjmabQdkM6wv83d0ywNfg1YBiLZJQnxrODz06NSrz8mz_I-5As9fEH-wgeupqQLavlZ7T1NC9WFHwX42Ng15hpvx3KBoOXPt7pv9gBqPJdXvJwNiXPsP7fFj2tvDoD4GzFHP60tamToxixpBd8H-Krv-YF3Bze0EGRZMCsUow6NiXgYaSGVLSrDlsZ35LwtU7ZJ84tjvwyu4lsL02kk-f1v8sZc7HySRVyl_TPfSCufjsmvcpl4LoCNs6GrSbvIsF4iEjb6qMJDcwRDhWFmkxaQXaqqqQ";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface UploadCallback {
        void onSuccess(String sharedUrl);
        void onError(String message);
    }

    /** Sube un archivo desde una Uri local (galería) a Dropbox y devuelve el link compartido. */
    public static void uploadFile(Context context, Uri fileUri, UploadCallback callback) {
        new Thread(() -> {
            try {
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

                RequestBody body = RequestBody.create(fileBytes, MediaType.parse("application/octet-stream"));

                Request uploadRequest = new Request.Builder()
                        .url("https://content.dropboxapi.com/2/files/upload")
                        .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
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
                        .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                        .addHeader("Content-Type", "application/json")
                        .post(RequestBody.create(sharedArg.toString(), MediaType.parse("application/json")))
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