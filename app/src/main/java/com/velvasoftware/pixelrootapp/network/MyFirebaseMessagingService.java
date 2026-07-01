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
import com.velvasoftware.pixelrootapp.R;

/**
 * ======================================================================================
 * GUÍA DE CONFIGURACIÓN DE FIREBASE CLOUD MESSAGING (FCM)
 * ======================================================================================
 * 1. GOOGLE-SERVICES.JSON: Descarga el archivo desde Firebase Console y ponlo en /app.
 * 2. MANIFEST: Registra este servicio:
 *    <service
 *        android:name=".network.MyFirebaseMessagingService"
 *        android:exported="false">
 *        <intent-filter>
 *            <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *        </intent-filter>
 *    </service>
 * 3. TOKEN: El token de registro se obtiene en onNewToken para guardarlo en la DB.
 * ======================================================================================
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            sendNotification(remoteMessage.getNotification().getTitle(), 
                             remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Enviar este token al servidor de backend para asociarlo al usuario
        super.onNewToken(token);
    }

    private void sendNotification(String title, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "PixelRootChannel";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.pixel_root_icono_claro)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
