package com.mnemo.angler.background_changer;

import android.content.Context;
import android.os.Bundle;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mnemo.angler.R;

import java.util.ArrayList;

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        ViewHolder(ImageView imageView){
            super(imageView);
            this.imageView = imageView;
        }
    }


    private Context context;
    private ArrayList<String> images;
    private String imageType;

    ImageFolderAdapter(Context context, ArrayList<String> images, String imageType) {
        this.context = context;
        this.images = images;
        this.imageType = imageType;
    }

    @Override
    public ImageFolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ImageView imageView = (ImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.bg_folder_image_item, parent, false);
        return new ImageFolderAdapter.ViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final int selectedImagePosition = position;

        final ImageView background = holder.imageView;
        background.setTransitionName(context.getResources().getString(R.string.local_load_image_transition) + selectedImagePosition);

        ImageAssistant.loadImage(context, images.get(position), background, 120);


        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageCarouselFragment imageCarouselFragment = new ImageCarouselFragment();
                Bundle args = new Bundle();
                args.putStringArrayList("images",images);
                args.putString("image",images.get(selectedImagePosition));
                args.putString("image_type", imageType);
                args.putInt("position", selectedImagePosition);
                imageCarouselFragment.setArguments(args);

                imageCarouselFragment.setSharedElementEnterTransition(new Fade());

                ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                        .addSharedElement(background, context.getResources().getString(R.string.local_load_image_transition) + selectedImagePosition)
                        .add(R.id.full_frame, imageCarouselFragment)
                        .addToBackStack(null)
                        .commit();

            }
        });

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}