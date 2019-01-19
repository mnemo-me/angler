package com.mnemo.angler.data.networking;


import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

interface LastFMApiService {


    @GET("?method=album.getinfo")
    Single<Response<ResponseBody>> getAlbum(@Query("api_key") String apiKey, @Query("artist") String artist, @Query("album") String album, @Query("format") String format);

    @GET("?method=artist.getInfo")
    Single<Response<ResponseBody>> getArtist(@Query("api_key") String apiKey, @Query("artist") String artist, @Query("format") String format);

    @Streaming
    @GET
    Single<Response<ResponseBody>> downloadImage(@Url String url);
}
