package com.mnemo.angler.background_changer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;

import java.io.File;
import java.util.ArrayList;

public class BackgroundImageAdapter extends RecyclerView.Adapter<BackgroundImageAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder{

        private CardView cardView;

        ViewHolder(CardView cardView){
            super(cardView);
            this.cardView = cardView;
        }
    }

    // Listener interfaces
    public interface OnImageClickListener{

        void onImageClick(String image);
    }


    private Context context;
    private ArrayList<String> images;
    private String selectedImage;
    private OnImageClickListener onImageClickListener;

    BackgroundImageAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public BackgroundImageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        CardView relativeLayout = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.bg_background_item, parent, false);
        return new BackgroundImageAdapter.ViewHolder(relativeLayout);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        ImageView background = holder.cardView.findViewById(R.id.background);

        final String image = images.get(position);

        if (image.equals(((Activity) context).getPreferences(Context.MODE_PRIVATE).getString("background", null))){
            holder.cardView.setActivated(true);
            if (image.equals(selectedImage)){
                holder.cardView.setSelected(true);
            }else{
                holder.cardView.setSelected(false);
            }
        }else{
            holder.cardView.setActivated(false);
            if (image.equals(selectedImage)){
                holder.cardView.setSelected(true);
            }else{
                holder.cardView.setSelected(false);
            }
        }

        final int orientation = context.getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ImageAssistent.loadImage(context, image, background, 100);
        } else {
            ImageAssistent.loadImage(context, image.replace("port", "land"), background, 120);
        }


        Group addImage = holder.cardView.findViewById(R.id.background_add_image);

        if (holder.getAdapterPosition() != 0) {
            if (addImage.getVisibility() == View.VISIBLE){
                addImage.setVisibility(View.GONE);
            }
        }else{
            addImage.setVisibility(View.VISIBLE);
        }

        background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.getAdapterPosition() == 0){

                    LocalLoadFragment localLoadFragment = new LocalLoadFragment();
                    Bundle args = new Bundle();
                    args.putString("image_type", "background");
                    localLoadFragment.setArguments(args);

                    ((MainActivity)context).getSupportFragmentManager().beginTransaction()
                            .add(R.id.full_frame, localLoadFragment)
                            .addToBackStack(null)
                            .commit();
                }else {
                    selectedImage = image;
                    notifyDataSetChanged();

                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        onImageClickListener.onImageClick(image);
                    }else{
                        onImageClickListener.onImageClick(image.replace("port", "land"));
                    }
                }
            }
        });

        // Setup delete image button
        ImageButton deleteImage = holder.cardView.findViewById(R.id.background_delete_image);

        if (image.startsWith("R.drawable.")){
            deleteImage.setVisibility(View.GONE);
        }else{
            if (deleteImage.getVisibility() == View.GONE){
                deleteImage.setVisibility(View.VISIBLE);
            }
        }

        deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File fileToDeletePort = new File(image);
                fileToDeletePort.delete();

                File fileToDeleteLand = new File(image.replace("port", "land"));
                fileToDeleteLand.delete();

                images.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }

        });



    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // Setters for listeners
    void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public String getSelectedImage() {
        return selectedImage;
    }
}