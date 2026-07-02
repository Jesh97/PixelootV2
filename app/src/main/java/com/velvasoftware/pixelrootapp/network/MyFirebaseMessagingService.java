package com.velvasoftware.pixelrootapp.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.velvasoftware.pixelrootapp.MainActivity;
import com.velvasoftware.pixelrootapp.MenuActivity;
import com.velvasoftware.pixelrootapp.R;
import com.velvasoftware.pixelrootapp.network.api.RetrofitClient;
import com.velvasoftware.pixelrootapp.network.api.UserApi;
import com.velvasoftware.pixelrootapp.network.request.FcmTokenRequest;
import com.velvasoftware.pixelrootapp.network.response.ApiResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_GENERAL = "pixelroot_general";
    private static final String CHANNEL_TICKETS = "pixelroot_tickets";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String type = data.getOrDefault("type", "");

        // El backend envía titulo/cuerpo en el campo notification + data para navigation
        String titulo;
        String cuerpo;

        if (remoteMessage.getNotification() != null) {
            titulo = remoteMessage.getNotification().getTitle();
            cuerpo = remoteMessage.getNotification().getBody();
        } else {
            // Fallback si llega como data-only
            titulo = data.getOrDefault("titulo", "Pixel Root");
            cuerpo = data.getOrDefault("cuerpo", "");
        }

        mostrarNotificacion(titulo, cuerpo, type);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SessionManager sm = SessionManager.getInstance(getApplicationContext());
        sm.saveFcmToken(token);
        // Si el usuario ya tiene sesión activa, enviar el token al servidor de inmediato
        if (sm.isLoggedIn()) {
            enviarTokenAlServidor(token);
        }
    }

    // ── Mostrar la notificación ───────────────────────────────────────────────

    private void mostrarNotificacion(String titulo, String cuerpo, String type) {
        Intent intent;
        int notifId;
        String channelId;

        switch (type) {
            case "ticket_estado":
            case "ticket_mensaje":
                // Abre la app en la sección de tickets
                intent    = new Intent(this, MenuActivity.class);
                intent.putExtra("nav_destino", "tickets");
                notifId   = 100;
                channelId = CHANNEL_TICKETS;
                break;

            case "nuevo_dispositivo":
                // Alerta de seguridad — abre la pantalla principal
                intent    = new Intent(this, MainActivity.class);
                notifId   = 101;
                channelId = CHANNEL_GENERAL;
                break;

            case "nuevo_juego":
                // Abre la app en el catálogo
                intent    = new Intent(this, MenuActivity.class);
                intent.putExtra("nav_destino", "catalogo");
                notifId   = 102;
                channelId = CHANNEL_GENERAL;
                break;

            default:
                intent    = new Intent(this, MainActivity.class);
                notifId   = 103;
                channelId = CHANNEL_GENERAL;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, notifId, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        crearCanal(channelId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.pixel_root_icono_claro)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(cuerpo))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(notifId, builder.build());
    }

    private void crearCanal(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = channelId.equals(CHANNEL_TICKETS)
                    ? "Tickets de soporte"
                    : "Notificaciones Pixel Root";
            NotificationChannel channel = new NotificationChannel(
                    channelId, name, NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }

    // ── Envío del token al servidor ───────────────────────────────────────────

    private void enviarTokenAlServidor(String token) {
        UserApi userApi = RetrofitClient.getUserApi();
        FcmTokenRequest req = new FcmTokenRequest(token, "Android " + Build.MODEL);
        userApi.registrarFcmToken(req).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call,
                                   @NonNull Response<ApiResponse<Void>> response) {
                // Token registrado correctamente
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, @NonNull Throwable t) {
                // Se reintentará en el próximo login
            }
        });
    }
}
