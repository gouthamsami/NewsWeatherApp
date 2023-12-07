package com.newsapp.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static final String News_URL = "https://api.spaceflightnewsapi.net/v4/";
    private static Retrofit retrofit;
    private static Retrofit retrofit_news;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


    public static Retrofit getnews() {
        if (retrofit_news == null) {
            retrofit_news = new Retrofit.Builder()
                    .baseUrl(News_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit_news;
    }
}
