package com.mnemo.angler.data.file_storage;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.data.database.Entities.Track;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class AnglerFileStorage {

    // Listener interfaces
    public interface OnGatherBackgroundImagesListener{
        void backgroundImagesGathered(List<String> images);
    }

    public interface OnImageFolderLoadListener{
        void onImageFolderLoaded(List<String> images);
    }

    public interface OnArtistBioLoadListener{
        void onArtistBioLoaded(String bio);
    }

    public interface OnBackgroundCheckListener{
        void onBackgroundChecked(String backgroundImage);
    }

    public static final String PHONE_STORAGE = Environment.getExternalStorageDirectory().getPath();
    private static final String TEMP_IMAGE_NAME = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + "temp.jpg";

    private Context context;

    @Inject
    public AnglerFileStorage(Context context) {

        this.context = context;
    }


    // Create Angler folder on device
    public void createAppFolder() {

        new File(AnglerFolder.PATH_MAIN).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_DEFAULT).mkdir();
        new File(AnglerFolder.PATH_PLAYLIST_COVER).mkdir();
        new File(AnglerFolder.PATH_ALBUM_COVER).mkdir();
        new File(AnglerFolder.PATH_ARTIST_IMAGE).mkdir();
        new File(AnglerFolder.PATH_ARTIST_BIO).mkdir();

        try {
            new File(AnglerFolder.PATH_MAIN, ".nomedia").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Recursively retrieve metadata from phone storage
    public ArrayList<Track> scanTracks(String filepath){

        ArrayList<Track> tracks = new ArrayList<>();

        File directory = new File(filepath);
        ArrayList<String> files = new ArrayList<>(Arrays.asList(directory.list()));

        if (directory.getName().startsWith(".") || tracks.contains(".nomedia")){
            return tracks;
        }

        MediaMetadataRetriever mRetriever = new MediaMetadataRetriever();

        for (String file : files) {

            File temp = new File(filepath + File.separator + file);

            if (temp.isDirectory()) {

                tracks.addAll(scanTracks(filepath + File.separator + file));

            } else {

                String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());

                if (extension.equals("")){
                    extension = file.substring(file.lastIndexOf(".") + 1);
                }

                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                if (mimeType != null) {
                    if (mimeType.startsWith("audio/")) {

                        try {
                            mRetriever.setDataSource(filepath + File.separator + file);
                        }catch (Exception e){
                            continue;
                        }

                        String title = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String album = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        long duration = Long.parseLong(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        String id = (title + "-" + artist + "-" + album).replace(" ", "_");
                        String uri = filepath + File.separator + file;

                        String yearString = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);

                        int year;

                        if (yearString != null) {
                            try {
                                year = Integer.parseInt(yearString);
                            }catch (NumberFormatException e){
                                year = 10000;
                            }
                        }else{
                            year = 10000;
                        }

                        String positionString = mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);

                        int position;

                        if (positionString != null){
                            position = Integer.parseInt(positionString.split("/")[0]);
                        }else{
                            position = 10000;
                        }

                        if (title != null && artist != null && album != null && duration != 0) {
                            tracks.add(new Track(id, title, artist, album, duration, uri, year, position));
                        }

                    }
                }
            }
        }

        return tracks;
    }

    // Get removable SD card path via reflections
    public String getRemovableSDCardPath(){

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);

                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);

                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
            return null;
    }


    // Get temp image name
    public String getTempImageName() {
        return TEMP_IMAGE_NAME;
    }

    // Get temp cover uri
    public Uri getTempCoverUri(){

        File destinationFile = new File(TEMP_IMAGE_NAME);

        try {
            destinationFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        return Uri.fromFile(destinationFile);
    }


    // Create temp image
    public void createTempImage(){

        File outputFile = new File(TEMP_IMAGE_NAME);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier("playlist_default", "drawable", context.getPackageName()));

        try {

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }



    // Copy images
    public void copyImage(String inputFileName, String outputFileName){

        File inputFile = new File(inputFileName);
        try {
            FileInputStream inputStream = new FileInputStream(inputFile);
            FileOutputStream outputStream = new FileOutputStream(outputFileName);

            byte[] buff = new byte[1024];
            int length;

            while ((length = inputStream.read(buff)) > 0){
                outputStream.write(buff,0, length);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    // Delete cover image
    public void deleteCoverImage(String playlist){

        String coverImage = generatePlaylistCoverImageName(playlist);

        new File(coverImage).delete();
    }

    // Delete background image
    public void deleteBackgroundImage(String image){

        File fileToDeletePort = new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT, image);
        fileToDeletePort.delete();

        File fileToDeleteLand = new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE, image);
        fileToDeleteLand.delete();
    }

    // Generate cover name
    public String generatePlaylistCoverImageName(String title){

        return AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg";
    }

    // Rename cover image
    public void renameCover(String oldImageName, String newImageName){

        File oldImage = new File(oldImageName);
        File newImage = new File(newImageName);

        if (oldImage.exists()){
            oldImage.renameTo(newImage);
        }
    }

    // Gather background images
    @SuppressLint("CheckResult")
    public void gatherBackgroundImages(OnGatherBackgroundImagesListener listener){

        Completable.fromAction(this::cleanImages)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {

                    String imageFolderPath = AnglerFolder.PATH_BACKGROUND_PORTRAIT;

                    File directory = new File(imageFolderPath);
                    String[] files = directory.list();

                    Observable.fromArray(files)
                            .filter(file -> {

                                File temp = new File(imageFolderPath + File.separator + file);
                                String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
                                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                                return mimeType != null && mimeType.contains("image/");

                            })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .toList()
                            .subscribe(images -> {

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    images.sort(Comparator.comparing(s -> - (new File(imageFolderPath + File.separator + s).lastModified())));
                                }else{
                                    Collections.sort(images, (image1, image2) -> Long.compare(new File(imageFolderPath + File.separator + image2).lastModified(),
                                    new File(imageFolderPath + File.separator + image1).lastModified()));
                                }

                                // Add default images to list
                                for (int i = 1; i <= 4; i++){
                                    if (isDefaultBackgroundExist("back" + i)){
                                        images.add(getDefaultBackgroundPath("back" + i));
                                    }
                                }


                                listener.backgroundImagesGathered(images);
                            });

                });

    }



    // Clean single orientation background images
    private void cleanImages(){

        File portDirectory = new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT);
        String[] portFiles = portDirectory.list();

        for (String portFile : portFiles){

            File port = new File(portFile);
            File land = new File(portFile.replace("port","land"));

            if (!land.exists()){
                port.delete();
            }
        }

        File landDirectory = new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE);
        String[] landFiles = landDirectory.list();

        for (String landFile : landFiles){

            File land = new File(landFile);
            File port = new File(landFile.replace("land","port"));

            if (!port.exists()){
                land.delete();
            }
        }
    }

    // Get artist image path
    public String getArtistImagePath(String artist){
        return AnglerFolder.PATH_ARTIST_IMAGE + File.separator + artist + ".jpg";
    }

    public Uri getArtistImageUri(String artist){
        return Uri.fromFile(new File(getArtistImagePath(artist)));
    }

    // Get artist bio path
    private String getArtistBioPath(String artist){
        return AnglerFolder.PATH_ARTIST_BIO + File.separator + artist + ".txt";
    }

    // Get album image path
    public String getAlbumImagePath(String artist, String album){
        return AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";
    }

    public Uri getAlbumImageUri(String artist, String album){
        return Uri.fromFile(new File(getAlbumImagePath(artist, album)));
    }

    // Get defalut background path
    public String getDefaultBackgroundPath(String background){
        return AnglerFolder.PATH_BACKGROUND_DEFAULT + File.separator + background + ".jpg";
    }

    public Uri getDefaultBackgroundUri(String background){
        return Uri.fromFile(new File(getDefaultBackgroundPath(background)));
    }


    // Create artist albums directory
    public void createArtistAlbumsDirectory(String artist){

        String path = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist;

        File artistAlbumsDirectory = new File(path);

        if (!artistAlbumsDirectory.exists()){
            artistAlbumsDirectory.mkdir();
        }
    }

    // Save album cover
    public Uri saveAlbumCover(String artist, String album, InputStream inputStream){

        String path = AnglerFolder.PATH_ALBUM_COVER + File.separator + artist;

        File file = new File(path, album + ".jpg");

        FileOutputStream outputStream = null;

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        } catch (IOException e) {

            e.printStackTrace();

        } finally {

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

        return Uri.fromFile(file);
    }

    // Save artist bio
    public void saveArtistBio(String artist, String bio){

        File file = new File(getArtistBioPath(artist));

        try {

            if (!file.exists()){
                file.createNewFile();
            }

            if (bio != null) {
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
                bufferedWriter.write(bio);
                bufferedWriter.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    // Check is files exist methods
    public boolean isAlbumCoverExist(String artist, String album){
        return new File(getAlbumImagePath(artist, album)).exists();
    }

    public boolean isArtistImageExist(String artist){
        return new File(getArtistImagePath(artist)).exists();
    }

    public boolean isArtistBioExist(String artist){
        return new File(getArtistBioPath(artist)).exists();
    }

    public boolean isDefaultBackgroundExist(String background){
        return new File(getDefaultBackgroundPath(background)).exists();
    }



    public InputStream extractAlbumImage(String uri) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);

        byte[] data = retriever.getEmbeddedPicture();

        if (data != null) {
            return new ByteArrayInputStream(data);
        }else {
            return null;
        }
    }


    @SuppressLint("CheckResult")
    public void loadArtistBio(String artist, OnArtistBioLoadListener listener){

        AtomicReference<String> bio = new AtomicReference<>();

        Completable.fromAction(() -> {

            StringBuilder builder = new StringBuilder();

            File file = new File(AnglerFolder.PATH_ARTIST_BIO, artist + ".txt");

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

                while (bufferedReader.ready()){
                    builder.append("\n").append(bufferedReader.readLine());
                }
                bufferedReader.close();

            }catch (IOException e){
                e.printStackTrace();
            }

            bio.set(builder.toString());
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> listener.onArtistBioLoaded(bio.get()));


    }

    /*
    Recursively collect all folders with images on device
    ignoring folders, contains .nomedia file and hidden folders
     */
    public List<String> gatherImageFolders(String path){

        List<String> imageFolders = new ArrayList<>();

        File directory = new File(path);
        ArrayList<String> files = new ArrayList<>(Arrays.asList(directory.list()));


        if (directory.getName().startsWith(".") || files.contains(".nomedia")){
            return imageFolders;
        }

        for (String file : files) {

            File temp = new File(path + File.separator + file);

            if (temp.isDirectory()) {

                imageFolders.addAll(gatherImageFolders(path + File.separator + file));

            }else if (!imageFolders.contains(path)){

                String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                if (mimeType != null) {
                    if (mimeType.contains("image/")) {

                        imageFolders.add(path);
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            imageFolders.sort(Comparator.comparing(s -> new File(s).getName()));
        }else{
            Collections.sort(imageFolders, (imageFolder1, imageFolder2) -> new File(imageFolder1).getName().compareTo(new File(imageFolder2).getName()));
        }

        return imageFolders;
    }

    // Get list of images in selected folder
    @SuppressLint("CheckResult")
    public void getImages(String imageFolder, OnImageFolderLoadListener listener) {

        File directory = new File(imageFolder);
        String[] files = directory.list();

        Observable.fromArray(files)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(file -> {

                    File temp = new File(imageFolder + File.separator + file);
                    String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                    return mimeType != null && mimeType.contains("image/");

                })
                .map(file -> imageFolder + File.separator + file)
                .toList()
                .subscribe(images -> {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        images.sort(Comparator.comparing(s -> - (new File(s).lastModified())));
                    }else{
                        Collections.sort(images, (image1, image2) -> Long.compare(new File(image2).lastModified(), new File(image1).lastModified()));
                    }

                    listener.onImageFolderLoaded(images);
                });


    }


    // Generate new name for image file
    public String generateNewImageName(String image){

        String newImageName = new File(image).getName();
        newImageName = newImageName.replace("R.drawable.","d");
        newImageName = newImageName.substring(0, newImageName.lastIndexOf("."));

        if (new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT,newImageName + ".jpeg").exists()){

            int count = 2;
            while (new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT, newImageName + "(" + count + ").jpeg").exists()){
                count++;
            }
            newImageName = newImageName + "(" + count + ")";
        }

        newImageName = newImageName + ".jpeg";

        return newImageName;
    }

    // Create new background image file
    public Uri getImageUri(String imageFileName, int orientation){

        String imageFolderPath;

        if (orientation == Configuration.ORIENTATION_PORTRAIT){
            imageFolderPath = AnglerFolder.PATH_BACKGROUND_PORTRAIT;
        }else{
            imageFolderPath = AnglerFolder.PATH_BACKGROUND_LANDSCAPE;
        }

        File destinationFile = new File(imageFolderPath, imageFileName);

        try {
            destinationFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }

        return Uri.fromFile(destinationFile);
    }

    // Check if file exist
    public boolean isFileExist(String filepath){
        return new File(filepath).exists();
    }

    // Check artist bio
    public boolean checkArtistBio(String artist){

        File file =  new File(getArtistBioPath(artist));

        if (!file.exists()){
            return false;
        }else{
            return file.length() > 0;
        }
    }


}
