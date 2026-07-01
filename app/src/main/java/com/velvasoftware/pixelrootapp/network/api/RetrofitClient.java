package com.velvasoftware.pixelrootapp.network.api;

import android.content.Context;

import com.velvasoftware.pixelrootapp.network.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Backend real (Render). Todas las rutas Flask están registradas bajo /api/...
    private static final String BASE_URL = "https://pixeloot-apis.onrender.com/api/";

    private static Retrofit retrofit = null;

    /**
     * Debe llamarse una vez (por ejemplo desde la Application o la primera Activity)
     * para que el cliente pueda leer el token guardado en SessionManager.
     */
    public static void init(Context context) {
        if (retrofit != null) return;

        SessionManager session = SessionManager.getInstance(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                // Timeouts altos porque Render (plan free) "duerme" el servicio tras inactividad
                // y la primera petición puede tardar 30-60s en despertarlo (cold start).
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = session.getToken();

                    if (token == null) {
                        return chain.proceed(original);
                    }

                    Request authenticated = original.newBuilder()
                            .header("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(authenticated);
                })
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private static Retrofit getClient() {
        if (retrofit == null) {
            throw new IllegalStateException(
                    "RetrofitClient.init(context) debe llamarse antes de usar cualquier API " +
                            "(por ejemplo en Application.onCreate o en la primera Activity)."
            );
        }
        return retrofit;
    }

    public static <T> T createService(Class<T> serviceClass) {
        return getClient().create(serviceClass);
    }

    public static AuthApi getAuthApi() { return createService(AuthApi.class); }
    public static CatalogApi getCatalogApi() { return createService(CatalogApi.class); }
    public static CartApi getCartApi() { return createService(CartApi.class); }
    public static OrderApi getOrderApi() { return createService(OrderApi.class); }
    public static TicketApi getTicketApi() { return createService(TicketApi.class); }
    public static BranchApi getBranchApi() { return createService(BranchApi.class); }
    public static NotificationApi getNotificationApi() { return createService(NotificationApi.class); }
    public static UserApi getUserApi() { return createService(UserApi.class); }
}