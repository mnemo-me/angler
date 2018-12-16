package com.mnemo.angler.data.networking;


import android.content.Context;
import android.util.Log;

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

    public interface OnAlbumYearLoadListener{
        void onAlbumYearLoaded(int year);
    }

    public interface OnArtistImageLoadListener{
        void onArtistImageLoaded(InputStream inputStream);
    }

    public interface OnArtistBioLoadListener{
        void onArtistBioLoaded(String bio);
    }

    public interface OnTrackAlbumPositionLoadListener{
        void onTrackAlbumPositionLoaded(int albumPosition);
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
                                    }else{
                                        // fix for singles
                                        if (album.contains("(Single)")) {
                                            loadAlbum(artist, album.replace(" (Single)", ""), listener);
                                        }
                                    }
                                });
                    }
                });
    }

    // Load album year
    public void loadAlbumYear(String artist, String album, OnAlbumYearLoadListener listener){

        lastFMApiService.getAlbum(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), album.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response.code() == 200) {

                        int year = getAlbumYear(response.body());

                        listener.onAlbumYearLoaded(year);
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
                                    }else{
                                        listener.onArtistImageLoaded(null);
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

    // Load track album position
    public void loadTrackAlbumPosition(String title, String artist, String album, OnTrackAlbumPositionLoadListener listener){

        lastFMApiService.getAlbum(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), album.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response.code() == 200){
                        int albumPosition = getTrackAlbumPosition(response.body(), title);
                        listener.onTrackAlbumPositionLoaded(albumPosition);
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

    // Parse JSON to get album year
    private int getAlbumYear(ResponseBody responseBody){

        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONObject typeJSONObject = jsonObject.getJSONObject("album");
            JSONObject wikiJSONObject = typeJSONObject.getJSONObject("wiki");

            String release = wikiJSONObject.getString("published");

            String yearString = release.substring(0, release.indexOf(","));
            String yearString2 = yearString.substring(yearString.lastIndexOf(" ") + 1);

            return Integer.parseInt(yearString2);

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return 10000;
    }

    // Parse JSON to get artist bio
    private String getArtistBio(ResponseBody responseBody){

        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONObject typeJSONObject = jsonObject.getJSONObject("artist");
            JSONObject bioJSONObject = typeJSONObject.getJSONObject("bio");

            return bioJSONObject.getString("content");

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return null;
    }

    // Parse JSON to get track album position
    private int getTrackAlbumPosition(ResponseBody responseBody, String title){

        try {
            JSONObject jsonObject = new JSONObject(responseBody.string());
            JSONObject typeJSONObject = jsonObject.getJSONObject("album");
            JSONObject tracksJSONObject = typeJSONObject.getJSONObject("tracks");
            JSONArray trackJSONArray = tracksJSONObject.getJSONArray("track");

            for (int i = 0; i < trackJSONArray.length(); i++){

                JSONObject trackJSONObject = trackJSONArray.getJSONObject(i);

                if (trackJSONObject.getString("name").equalsIgnoreCase(title)) {

                    JSONObject attrJSONObject = trackJSONObject.getJSONObject("@attr");

                    return Integer.parseInt(attrJSONObject.getString("rank"));
                }
            }

            return 10000;

        }catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return 10000;
    }


}
