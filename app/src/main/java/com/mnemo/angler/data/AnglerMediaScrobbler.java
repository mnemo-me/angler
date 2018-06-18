package com.mnemo.angler.data;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class AnglerMediaScrobbler {

    private static String lastFMAPIKey = "28c57911603d63ce40347e8a4bdb04f7";

    private static class LoadMediaImageAsyncTask extends AsyncTask<URL, Void, Void>{

        String stringUrl;
        String path;
        String fileName;
        String type;
        Bundle args;

        public LoadMediaImageAsyncTask(String stringUrl, String path, String fileName, String type, Bundle args) {
            this.stringUrl = stringUrl;
            this.path = path;
            this.fileName = fileName;
            this.type = type;
            this.args = args;
        }

        @Override
        protected Void doInBackground(URL... urls) {
            URL url;
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;

            new File(path).mkdir();
            File file = new File(path, fileName);

            if (!file.exists()) {

                try {
                    url = new URL(stringUrl);
                } catch (MalformedURLException e) {
                    url = null;
                }


                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(15000);
                    urlConnection.connect();

                    if (urlConnection.getResponseCode() == 200) {

                        inputStream = urlConnection.getInputStream();
                        String JSONResponse = getJSONResponse(inputStream);

                        String imageUrlString = parseJSON(JSONResponse, type);

                        if (TextUtils.isEmpty(imageUrlString) && type.equals("album")) {

                            String uri = args.getString("uri");

                            extractAlbumImage(uri, file);

                        } else {

                            uploadImage(imageUrlString, file);
                        }

                    }


                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }

                    if (inputStream != null) {

                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return null;
        }
    }



    public static void getAlbumCover(String artist, String album, String uri){

        Uri baseUri = Uri.parse("http://ws.audioscrobbler.com/2.0/?method=album.getinfo");
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", lastFMAPIKey);
        uriBuilder.appendQueryParameter("artist", artist.replace("&", "and"));
        uriBuilder.appendQueryParameter("album", album.replace("&", "and"));
        uriBuilder.appendQueryParameter("format", "json");
        String stringUrl = uriBuilder.toString();

        String path = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist;
        String fileName = album + ".jpg";

        Bundle args = new Bundle();
        args.putString("uri", uri);

        LoadMediaImageAsyncTask task = new LoadMediaImageAsyncTask(stringUrl, path, fileName, "album", args);
        task.execute();

    }

    public static void getArtistImage(String artist){

        Uri baseUri = Uri.parse("http://ws.audioscrobbler.com/2.0/?method=artist.getinfo");
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("api_key", lastFMAPIKey);
        uriBuilder.appendQueryParameter("artist", artist.replace("&", "and"));
        uriBuilder.appendQueryParameter("format", "json");
        String stringUrl = uriBuilder.toString();

        String path = AnglerFolder.PATH_ARTIST_IMAGE;
        String fileName = artist + ".jpg";

        LoadMediaImageAsyncTask task = new LoadMediaImageAsyncTask(stringUrl, path, fileName, "artist", null);
        task.execute();
    }


    // Helper method to decode JSON from input stream
    public static String getJSONResponse(InputStream inputStream){

        StringBuilder builder = new StringBuilder();

        if (inputStream != null){

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            try {
                String line = reader.readLine();

                while (line != null){
                    builder.append(line);
                    line = reader.readLine();
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }


        return builder.toString();
    }

    public static String parseJSON(String JSONResponse, String type){

        try {
            JSONObject jsonObject = new JSONObject(JSONResponse);
            JSONObject albumJSONObject = jsonObject.getJSONObject(type);
            JSONArray albumImagesJSONArray = albumJSONObject.getJSONArray("image");

            String imageUrlString = "";

            for (int i = 0; i < albumImagesJSONArray.length(); i++){

                if (albumImagesJSONArray.getJSONObject(i).getString("size").equals("mega")){
                    imageUrlString = albumImagesJSONArray.getJSONObject(i).getString("#text");
                    break;
                }
            }

            return imageUrlString;

        }catch (JSONException e){
            e.printStackTrace();
        }

        return null;
    }


    public static void uploadImage(String imageUrlString, File file){

        if (!TextUtils.isEmpty(imageUrlString)){

            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            FileOutputStream outputStream = null;

            try {
                URL imageUrl = new URL(imageUrlString);
                urlConnection = (HttpURLConnection) imageUrl.openConnection();
                inputStream = urlConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);


            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }


        }
    }

    public static void extractAlbumImage(String uri, File file) {

        if (!file.exists()) {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(uri);

            byte[] data = retriever.getEmbeddedPicture();

            if (data != null) {

                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                FileOutputStream outputStream = null;

                try {
                    outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {

                    if (outputStream != null) {
                        try {
                            outputStream.flush();
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }


}
