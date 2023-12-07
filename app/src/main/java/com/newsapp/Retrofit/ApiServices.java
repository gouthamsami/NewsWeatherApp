package com.newsapp.Retrofit;


import okhttp3.ResponseBody;
import retrofit2.Call;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface ApiServices {

    @Headers("Content-Type: application/json") // Specify the content type as JSON
    @GET("weather")
    Call<ResponseBody> get_weather(@Query("lat") String lat,
                                    @Query("lon") String lon,
                                    @Query("appid") String key);

    @Headers("Content-Type: application/json")
    @GET("articles/")
    Call<ResponseBody> getnews(@Query("format") String format,
                                    @Query("limit") String limit,
                                    @Query("offset") String offset);

//    https://api.openweathermap.org/data/2.5/weather?lat={lat}&lon={lon}&appid={API key}

//    https://api.spaceflightnewsapi.net/v4/articles/?format=json&limit=20&offset=0





}

