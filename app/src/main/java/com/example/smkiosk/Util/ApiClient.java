package com.example.smkiosk.Util;

import com.example.smkiosk.BuildConfig;
import com.example.smkiosk.api.ApiService;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    //"http://10.0.2.2:8080/api/v1/";


    private static ApiService api;

    public static ApiService getApi() {
        if (api == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request req = chain.request().newBuilder()
                                .header("X-DEV-PASS", BuildConfig.API_KEY)
                                .build();
                        return chain.proceed(req);
                    })
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            api = retrofit.create(ApiService.class);
        }
        return api;
    }
}

