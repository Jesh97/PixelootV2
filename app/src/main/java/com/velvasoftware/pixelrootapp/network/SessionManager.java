package com.velvasoftware.pixelrootapp.network;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Guarda el token JWT y los datos básicos del usuario logueado usando SharedPreferences.
 * RetrofitClient lo usa para inyectar "Authorization: Bearer <token>" en cada request.
 */
public class SessionManager {

    private static final String PREFS_NAME = "pixelroot_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "usuario_id";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_APELLIDO = "apellido";
    private static final String KEY_CORREO = "correo";
    private static final String KEY_ROL_ID = "rol_id";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_FCM_TOKEN   = "fcm_token";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveSession(String token, int usuarioId, String nombre, String apellido,
                            String correo, int rolId, boolean rememberMe) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, usuarioId)
                .putString(KEY_NOMBRE, nombre)
                .putString(KEY_APELLIDO, apellido)
                .putString(KEY_CORREO, correo)
                .putInt(KEY_ROL_ID, rolId)
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .apply();
    }

    /** true si el usuario marcó "Recordarme" en el login: permite saltar el login la próxima vez que se abra la app. */
    public boolean isRememberMe() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }

    public int getUsuarioId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public String getApellido() {
        return prefs.getString(KEY_APELLIDO, "");
    }

    public String getCorreo() {
        return prefs.getString(KEY_CORREO, "");
    }

    public int getRolId() {
        return prefs.getInt(KEY_ROL_ID, -1);
    }

    public void saveFcmToken(String token) {
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply();
    }

    public String getFcmToken() {
        return prefs.getString(KEY_FCM_TOKEN, null);
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}