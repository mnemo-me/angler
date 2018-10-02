package com.mnemo.angler.utils;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.bumptech.glide.signature.ObjectKey;
import com.mnemo.angler.ui.main_activity.activity.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageAssistant {

    // Simplify loading image in ImageView
    public static void loadImage(Context context, String image, ImageView targetImageView, int imageHeight){

        // Calculate scale for image to reduce memory consumption
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        // Identify is current image default or local
        int resourceId = 0;

        if (image.startsWith("R.drawable.")){
            resourceId = context.getResources().getIdentifier(image.replace("R.drawable.",""),"drawable", context.getPackageName());
            BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        }else {
            BitmapFactory.decodeFile(image, options);
        }

        double width = options.outWidth;
        double height = options.outHeight;

        double scale = height / (double)(imageHeight * MainActivity.density);

        int scaleWidth = (int)(width / scale);
        int scaleHeight = (int)(height / scale);

        // Load image with Glide
        if (image.startsWith("R.drawable.")){
            GlideApp.with(context)
                    .load(resourceId)
                    .thumbnail(0.2f)
                    .override(scaleWidth, scaleHeight)
                    .into(targetImageView);
        }else {

            GlideApp.with(context)
                    .load(image)
                    .thumbnail(0.2f)
                    .override(scaleWidth, scaleHeight)
                    .signature(new ObjectKey(new File(image).lastModified()))
                    .into(targetImageView);
        }
    }


}
