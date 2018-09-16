package com.mnemo.angler.local_load;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.Fade;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.ImageAssistant;

import java.util.ArrayList;

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder{

        ViewHolder(View view){
            super(view);
        }
    }


    private Context context;
    private ArrayList<String> images;

    ImageFolderAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    @NonNull
    public ImageFolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ll_folder_image_item, parent, false);
        return new ImageFolderAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        holder.itemView.setTransitionName(context.getResources().getString(R.string.local_load_image_transition) + holder.getAdapterPosition());

        ImageAssistant.loadImage(context, images.get(position), (ImageView)holder.itemView, 120);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageCarouselFragment imageCarouselFragment = new ImageCarouselFragment();
                Bundle args = new Bundle();
                args.putStringArrayList("images",images);
                args.putString("image",images.get(holder.getAdapterPosition()));
                args.putInt("position", holder.getAdapterPosition());
                imageCarouselFragment.setArguments(args);

                imageCarouselFragment.setSharedElementEnterTransition(new Fade());

                ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                        .addSharedElement(holder.itemView, context.getResources().getString(R.string.local_load_image_transition) + holder.getAdapterPosition())
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