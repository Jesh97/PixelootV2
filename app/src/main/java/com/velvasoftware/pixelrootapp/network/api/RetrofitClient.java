package com.velvasoftware.pixelrootapp.network.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.pixelroot.com/v1/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static <T> T createService(Class<T> serviceClass) {
        return getClient().create(serviceClass);
    }
    
    // Métodos de conveniencia para obtener las instancias de las APIs
    public static AuthApi getAuthApi() { return createService(AuthApi.class); }
    public static CatalogApi getCatalogApi() { return createService(CatalogApi.class); }
    public static OrderApi getOrderApi() { return createService(OrderApi.class); }
    public static TicketApi getTicketApi() { return createService(TicketApi.class); }
    public static BranchApi getBranchApi() { return createService(BranchApi.class); }
    public static NotificationApi getNotificationApi() { return createService(NotificationApi.class); }
    public static UserApi getUserApi() { return createService(UserApi.class); }
}
