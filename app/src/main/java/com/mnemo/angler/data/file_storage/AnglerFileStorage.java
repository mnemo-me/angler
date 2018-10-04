package com.mnemo.angler.data.file_storage;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.mnemo.angler.data.database.Entities.Track;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

public class AnglerFileStorage {

    public static final String PHONE_STORAGE = Environment.getExternalStorageDirectory().getPath();
    public static final String TEMP_IMAGE_NAME = AnglerFolder.PATH_PLAYLIST_COVER + File.separator + "temp.jpg";

    Context context;

    @Inject
    public AnglerFileStorage(Context context) {

        this.context = context;
    }


    public void createAppFolder() {

        new File(AnglerFolder.PATH_MAIN).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_PORTRAIT).mkdir();
        new File(AnglerFolder.PATH_BACKGROUND_LANDSCAPE).mkdir();
        new File(AnglerFolder.PATH_PLAYLIST_COVER).mkdir();
        new File(AnglerFolder.PATH_ALBUM_COVER).mkdir();
        new File(AnglerFolder.PATH_ARTIST_IMAGE).mkdir();

        try {
            new File(AnglerFolder.PATH_MAIN, ".nomedia").createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isFileExist(String filepath){
        return new File(filepath).exists();
    }

    // recursively retrieve metadata from phone storage
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

                        tracks.add(new Track(id, title, artist, album, duration, uri));

                    }
                }
            }
        }

        return tracks;
    }

    // get temp image name
    public static String getTempImageName() {
        return TEMP_IMAGE_NAME;
    }


    // create temp image
    public void createTempImage(){

        File outputFile = new File(TEMP_IMAGE_NAME);
        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier("back3", "drawable", context.getPackageName()));

        try {

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }



    // copy images
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

        File fileToDeletePort = new File(image);
        fileToDeletePort.delete();

        File fileToDeleteLand = new File(image.replace("port", "land"));
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
    public List<String> gatherBackgroundImages(){

        cleanImages();

        String imageFolderPath = AnglerFolder.PATH_BACKGROUND_PORTRAIT;

        ArrayList<String> images = new ArrayList<>();

        File directory = new File(imageFolderPath);
        String[] files = directory.list();

        for (String file : files) {

            File temp = new File(imageFolderPath + File.separator + file);
            String extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(temp).toString());
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (mimeType != null) {
                if (mimeType.contains("image/")) {
                    images.add(imageFolderPath + File.separator + file);
                }
            }
        }

        return sortBackgroundImages(images);
    }

    private List<String> sortBackgroundImages(List<String> images){

        images.sort((image1, image2) -> {

            File one = new File(image1);
            File two = new File(image2);

            return (int)(two.lastModified() - one.lastModified());
        });

        return images;
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

    // Get album image path
    public String getAlbumImagePath(String artist, String album){
        return AnglerFolder.PATH_ALBUM_COVER + File.separator + artist + File.separator + album + ".jpg";
    }

}
