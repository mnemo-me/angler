package com.mnemo.angler.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.signature.ObjectKey;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ImageAssistant {

    // Simplify loading image in ImageView
    @SuppressLint("CheckResult")
    public static void loadImage(Context context, String image, ImageView targetImageView, int imageHeight){

        AtomicInteger resourceId = new AtomicInteger();

        AtomicInteger scaleWidth = new AtomicInteger();
        AtomicInteger scaleHeight = new AtomicInteger();

        Completable.fromAction(() -> {

            // Calculate scale for image to reduce memory consumption
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            // Identify is current image default or local
            if (image.startsWith("R.drawable.")){
                resourceId.set(context.getResources().getIdentifier(image.replace("R.drawable.", ""), "drawable", context.getPackageName()));
                BitmapFactory.decodeResource(context.getResources(), resourceId.get(), options);
            }else {
                BitmapFactory.decodeFile(image, options);
            }

            double width = options.outWidth;
            double height = options.outHeight;

            double scale = height / (imageHeight * MainActivity.density);

            scaleWidth.set((int) (width / scale));
            scaleHeight.set((int) (height / scale));



        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {

                    // Load image with Glide
                    if (image.startsWith("R.drawable.")){
                        GlideApp.with(context)
                                .load(resourceId.get())
                                .override(scaleWidth.get(), scaleHeight.get())
                                .into(targetImageView);
                    }else {

                        GlideApp.with(context)
                                .load(image)
                                .override(scaleWidth.get(), scaleHeight.get())
                                .signature(new ObjectKey(new File(image).lastModified()))
                                .into(targetImageView);
                    }
                });
    }


    // Simplify loading image in ImageView (main thread)
    @SuppressLint("CheckResult")
    public static void loadImageOnMainThread(Context context, String image, ImageView targetImageView, int imageHeight){

        int resourceId = 0;

        int scaleWidth;
        int scaleHeight;

        // Calculate scale for image to reduce memory consumption
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Identify is current image default or local
        if (image.startsWith("R.drawable.")){
            resourceId = context.getResources().getIdentifier(image.replace("R.drawable.", ""), "drawable", context.getPackageName());
            BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        }else {
            BitmapFactory.decodeFile(image, options);
        }

        double width = options.outWidth;
        double height = options.outHeight;

        double scale = height / (imageHeight * MainActivity.density);

        scaleWidth = (int) (width / scale);
        scaleHeight = (int) (height / scale);


        // Load image with Glide
        if (image.startsWith("R.drawable.")){
            GlideApp.with(context)
                    .load(resourceId)
                    .override(scaleWidth, scaleHeight)
                    .into(targetImageView);
        }else {

            GlideApp.with(context)
                    .load(image)
                    .override(scaleWidth, scaleHeight)
                    .signature(new ObjectKey(new File(image).lastModified()))
                    .into(targetImageView);
        }

    }

    // Simplify loading cover
    public static Bundle loadCoverImage(Context context, String image, ImageView targetImageView){

        int scaleWidth;
        int scaleHeight;

        // Calculate scale for image to reduce memory consumption
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image, options);


        double width = options.outWidth;
        double height = options.outHeight;

        double imageRation = height / width;

        double scale;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){

            if (imageRation > 1){
                scale = width / (context.getResources().getConfiguration().screenWidthDp * MainActivity.density- (int)(16 * MainActivity.density));
            }else{
                scale = width / (context.getResources().getConfiguration().screenWidthDp * MainActivity.density);
            }

        }else{
            if (imageRation > 1){
                scale = height / (context.getResources().getConfiguration().screenHeightDp * MainActivity.density);
            }else {
                scale = height / (context.getResources().getConfiguration().screenHeightDp * MainActivity.density - (int) (16 * MainActivity.density));
            }
        }

        scaleWidth = (int) (width / scale);
        scaleHeight = (int) (height / scale);

        // Load image with Glide
        GlideApp.with(context)
                .load(image)
                .override(scaleWidth, scaleHeight)
                .signature(new ObjectKey(new File(image).lastModified()))
                .into(targetImageView);

        Bundle imageSize = new Bundle();
        imageSize.putInt("image_height", scaleHeight);
        imageSize.putInt("image_width", scaleWidth);

        return imageSize;

    }

}
