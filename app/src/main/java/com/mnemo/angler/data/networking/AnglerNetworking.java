package com.mnemo.angler.data.networking;


import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class AnglerNetworking {

    // Listener interfaces
    public interface OnAlbumYearLoadListener{
        void onAlbumYearLoaded(int year);
    }
    public interface OnArtistBioLoadListener{
        void onArtistBioLoaded(String bio);
    }

    public interface OnTrackAlbumPositionLoadListener{
        void onTrackAlbumPositionLoaded(int albumPosition);
    }

    private Context context;

    private LastFMApiService lastFMApiService;

    @Inject
    public AnglerNetworking(Context context) {

        this.context = context;

        lastFMApiService = LastFMApiClient.getClient(context).create(LastFMApiService.class);
    }

    // Check network connection
    public boolean checkNetworkConnection(){

        boolean networkConnectionStatus;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        networkConnectionStatus = networkInfo != null && networkInfo.isConnected();

        //Check Wi Fi
        if (networkConnectionStatus) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                if (wifiInfo.getNetworkId() == -1){

                    networkConnectionStatus = false;
                }else {
                    int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);

                    if (level < 3) {
                        networkConnectionStatus = false;
                    }
                }
            }
        }

        return networkConnectionStatus;
    }

    // Load album year
    @SuppressLint("CheckResult")
    public void loadAlbumYear(String artist, String album, OnAlbumYearLoadListener listener){

        lastFMApiService.getAlbum(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), album.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response.code() == 200) {

                        int year = getAlbumYear(response.body());

                        listener.onAlbumYearLoaded(year);
                    }else{
                        // fix for singles, EP
                        if (album.contains("(Single)")) {
                            loadAlbumYear(artist, album.replace(" (Single)", ""), listener);
                        }else if (album.contains("(EP)")){
                            loadAlbumYear(artist, album.replace(" (EP)", ""), listener);
                        }
                    }
                });
    }


    // Load artist bio from LastFM
    @SuppressLint("CheckResult")
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
    @SuppressLint("CheckResult")
    public void loadTrackAlbumPosition(String title, String artist, String album, OnTrackAlbumPositionLoadListener listener){

        lastFMApiService.getAlbum(Net.LAST_FM_API_KEY,
                artist.replace("&", "and"), album.replace("&", "and"), "json")
                .subscribeOn(Schedulers.io())
                .subscribe(response -> {

                    if (response.code() == 200){
                        int albumPosition = getTrackAlbumPosition(response.body(), title);
                        listener.onTrackAlbumPositionLoaded(albumPosition);
                    }else{
                        // fix for singles, EP
                        if (album.contains("(Single)")) {
                            loadTrackAlbumPosition(title, artist, album.replace(" (Single)", ""), listener);
                        }else if (album.contains("(EP)")){
                            loadTrackAlbumPosition(title, artist, album.replace(" (EP)", ""), listener);
                        }
                    }
                });
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
