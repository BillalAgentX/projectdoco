package com.projectdocupro.mobile.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitManager {

    private static Retrofit retrofit;
    static OkHttpClient okHttpClient;

    public static Retrofit getInstance() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

//            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);



            okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(3 * 60, TimeUnit.SECONDS)
                    .connectTimeout(3 * 60, TimeUnit.SECONDS)
//                     .addInterceptor(interceptor)
                    .build();


            retrofit = new Retrofit.Builder()
                 // .baseUrl("https://mobileapi.projectdocu.net/")//actualLive
                   //.baseUrl("https://webapp.dev.projectdocu.net/")//demo server
                   .baseUrl("https://mobileapi.dev.projectdocu.net/")//demo app
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(okHttpClient)
                    .build();
            return retrofit;
        } else {
            return retrofit;
        }
    }
}
// .baseUrl("https://devapi.projectdocu.net/")