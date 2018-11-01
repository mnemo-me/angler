package com.mnemo.angler.data.networking;


import android.content.Context;
import android.util.Log;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

class LastFMApiClient {

    private static Retrofit retrofit = null;
    private static int REQUEST_TIMEOUT = 60;
    private static OkHttpClient okHttpClient;

    // Get singleton retrofit client
    static Retrofit getClient(Context context){

        if (okHttpClient == null){
            initOkHttp(context);
        }

        if (retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(Net.BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }

        return retrofit;
    }

    // Initialize OkHttp client
    private static void initOkHttp(Context context){

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

        // Add logging interceptor to watch requests
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        builder.addInterceptor(interceptor);

        builder.addInterceptor(chain -> {
            Request request = chain.request();
            Log.e("request", request.url().toString());
            return chain.proceed(request);
        });

        okHttpClient = builder.build();
    }
}
