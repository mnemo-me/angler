package com.mnemo.angler.data.networking;


import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class AnglerNetworking {

    // Listener interfaces
    public interface OnAlbumLoadListener{
        void onAlbumLoaded(InputStream inputStream);
    }

    public interface OnArtistImageLoadListener{
        void onArtistImageLoaded(InputStream inputStream);
    }

    public interface OnArtistBioLoadListener{
        void onArtistBioLoaded(String bio);
    }

    private LastFMApiService lastFMApiService;

    @Inject
    public AnglerNetworking(Context context) {

        lastFMApiService = LastFMApiClient.getClient(context).create(LastFMApiService.class);
    }

    // Load album cover from LastFM
    public void loadAlbum(String artist, String album, OnAlbumLoadListener listener){

        lastFMApiService.getAlbum(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), album.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response1 -> {

                    if (response1.code() == 200) {
                        String albumImageUrl = getImageUrl(response1.body(), "album");
                        lastFMApiService.downloadImage(albumImageUrl)
                                .subscribe(response2 -> {

                                    if (response2.code() == 200) {
                                        listener.onAlbumLoaded(response2.body().byteStream());
                                    }
                                });
                    }
                });
    }


    // Load artist image from LastFM
    public void loadArtistImage(String artist, OnArtistImageLoadListener listener){

        lastFMApiService.getArtist(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response1 -> {

                    if (response1.code() == 200){
                        String artistImageUrl = getImageUrl(response1.body(), "artist");
                        lastFMApiService.downloadImage(artistImageUrl)
                                .subscribe(response2 -> {

                                    if (response2.code() == 200){
                                        listener.onArtistImageLoaded(response2.body().byteStream());
                                    }
                                });
                    }
                });


    }

    // Load artist bio from LastFM
    public void loadArtistBio(String artist, OnArtistBioLoadListener listener){

        lastFMApiService.getArtist(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response1 -> {

                   if (response1.code() == 200){
                       String artistBio = getArtistBio(response1.body());
                       listener.onArtistBioLoaded(artistBio);
                   }
                });
    }

    // Parse JSON to get image url (album or artist)
    private String getImageUrl(ResponseBody responseBody, String type){

        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONObject typeJSONObject = jsonObject.getJSONObject(type);
            JSONArray imagesJSONArray = typeJSONObject.getJSONArray("image");

            String imageUrlString = "";

            for (int i = 0; i < imagesJSONArray.length(); i++){

                if (imagesJSONArray.getJSONObject(i).getString("size").equals("mega")){
                    imageUrlString = imagesJSONArray.getJSONObject(i).getString("#text");
                    break;
                }
            }

            return imageUrlString;

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return null;
    }


    // Parse JSON to get artist bio
    private String getArtistBio(ResponseBody responseBody){

        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONObject typeJSONObject = jsonObject.getJSONObject("artist");
            JSONObject bioJSONOnject = typeJSONObject.getJSONObject("bio");

            return bioJSONOnject.getString("content");

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return null;
    }


}