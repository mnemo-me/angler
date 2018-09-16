package com.mnemo.angler.background_changer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;
import com.mnemo.angler.data.ImageAssistant;
import com.mnemo.angler.local_load.LocalLoadActivity;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BackgroundImageAdapter extends RecyclerView.Adapter<BackgroundImageAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.background)
        ImageView background;

        @BindView(R.id.background_add_image)
        Group addImage;

        @BindView(R.id.background_delete_image)
        ImageButton deleteImage;

        ViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // Listener interfaces
    public interface OnImageClickListener{

        void onImageClick(String image);
    }


    private Context context;
    private ArrayList<String> images;
    private String currentBackground;
    private String selectedImage;
    private OnImageClickListener onImageClickListener;

    private int orientation;

    BackgroundImageAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;

        currentBackground = ((Activity) context).getPreferences(Context.MODE_PRIVATE).getString("background", null);
        orientation = context.getResources().getConfiguration().orientation;
    }

    @Override
    @NonNull
    public BackgroundImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bg_background_item, parent, false);
        return new BackgroundImageAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final String image = images.get(position);

        if (image.equals(currentBackground)){
            holder.itemView.setActivated(true);
        }else{
            holder.itemView.setActivated(false);
        }

        if (image.equals(selectedImage)){
            holder.itemView.setSelected(true);
        }else{
            holder.itemView.setSelected(false);
        }


        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            ImageAssistant.loadImage(context, image, holder.background, 120);
        } else {
            ImageAssistant.loadImage(context, image.replace("port", "land"), holder.background, 120);
        }


        if (holder.getAdapterPosition() != 0) {
            if (holder.addImage.getVisibility() == View.VISIBLE){
                holder.addImage.setVisibility(View.GONE);
            }
        }else{
            holder.addImage.setVisibility(View.VISIBLE);
        }

        holder.background.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (holder.getAdapterPosition() == 0){

                    Intent intent = new Intent(context, LocalLoadActivity.class);
                    intent.putExtra("image_type", "background");

                    context.startActivity(intent);

                }else {

                    selectedImage = image;
                    notifyDataSetChanged();

                    onImageClickListener.onImageClick(image);
                }
            }
        });

        // Setup delete image button
        if (image.startsWith("R.drawable.")){
            holder.deleteImage.setVisibility(View.GONE);
        }else{
            if (holder.deleteImage.getVisibility() == View.GONE){
                holder.deleteImage.setVisibility(View.VISIBLE);
            }
        }

        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File fileToDeletePort = new File(image);
                fileToDeletePort.delete();

                File fileToDeleteLand = new File(image.replace("port", "land"));
                fileToDeleteLand.delete();

                String backgroundImage = ((Activity) context).getPreferences(Context.MODE_PRIVATE).getString("background", "R.drawable.back");

                if (!backgroundImage.startsWith("R.drawable.")) {
                    if (!new File(backgroundImage).exists()) {
                        backgroundImage = "R.drawable.back";
                        onImageClickListener.onImageClick(backgroundImage);
                        ImageView background = ((MainActivity) context).findViewById(R.id.main_fragment_background);

                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                            ImageAssistant.loadImage(context, backgroundImage, background, 520);
                        } else {
                            ImageAssistant.loadImage(context, backgroundImage.replace("port", "land"), background, 203);
                        }
                    }
                }

                if (images.get(holder.getAdapterPosition()).equals(selectedImage)){
                    onImageClickListener.onImageClick(backgroundImage);
                }


                images.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }

        });



    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // Setter for listener
    void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    String getSelectedImage() {
        return selectedImage;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }
}