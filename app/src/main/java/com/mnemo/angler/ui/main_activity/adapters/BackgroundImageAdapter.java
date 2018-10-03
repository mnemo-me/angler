package com.mnemo.angler.ui.main_activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mnemo.angler.R;
import com.mnemo.angler.util.ImageAssistant;
import com.mnemo.angler.ui.local_load_activity.activity.LocalLoadActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BackgroundImageAdapter extends RecyclerView.Adapter<BackgroundImageAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class AddImageViewHolder extends ViewHolder{

        AddImageViewHolder(View view){
            super(view);
        }
    }

    static class ImageViewHolder extends ViewHolder{

        @BindView(R.id.background)
        ImageView background;

        @BindView(R.id.background_delete_image)
        ImageButton deleteImage;

        ImageViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    // Listener interfaces
    public interface OnImageClickListener{

        void onImageClick(String image);
    }

    public interface OnImageDeleteListener{

        void onImageDelete(String image);
    }


    private Context context;
    private int orientation;
    private List<String> images;
    private String currentBackground;
    private String selectedImage;

    private OnImageClickListener onImageClickListener;
    private OnImageDeleteListener onImageDeleteListener;

    private static final int ADD_IMAGE_VIEW_TYPE = 0;
    private static final int IMAGE_VIEW_TYPE = 1;

    public BackgroundImageAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;

        orientation = context.getResources().getConfiguration().orientation;
    }

    @Override
    @NonNull
    public BackgroundImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ADD_IMAGE_VIEW_TYPE){

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bg_background_add_image_item, parent, false);
            return new BackgroundImageAdapter.AddImageViewHolder(view);

        }else {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bg_background_item, parent, false);
            return new BackgroundImageAdapter.ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        if (holder.getItemViewType() == ADD_IMAGE_VIEW_TYPE){

            // Set listener (open local load activity)
            holder.itemView.setOnClickListener(view -> {

                Intent intent = new Intent(context, LocalLoadActivity.class);
                intent.putExtra("image_type", "background");

                context.startActivity(intent);
            });

        }else{

            String image = images.get(position - 1);

            // Activate if current background
            if (image.equals(currentBackground)){
                holder.itemView.setActivated(true);
            }else{
                holder.itemView.setActivated(false);
            }

            // Select if selected image
            if (image.equals(selectedImage)){
                holder.itemView.setSelected(true);
            }else{
                holder.itemView.setSelected(false);
            }

            // Load background image
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                ImageAssistant.loadImage(context, image, ((ImageViewHolder)holder).background, 120);
            } else {
                ImageAssistant.loadImage(context, image.replace("port", "land"), ((ImageViewHolder)holder).background, 120);
            }

            // Set listener (change image)
            holder.itemView.setOnClickListener(v -> {

                selectedImage = image;
                notifyDataSetChanged();

                onImageClickListener.onImageClick(image);
            });

            // Setup delete image button
            // Set visibility
            if (image.startsWith("R.drawable.")){

                ((ImageViewHolder)holder).deleteImage.setVisibility(View.GONE);

            }else{

                if (((ImageViewHolder)holder).deleteImage.getVisibility() == View.GONE){
                    ((ImageViewHolder)holder).deleteImage.setVisibility(View.VISIBLE);
                }
            }

            // Set listener
            ((ImageViewHolder)holder).deleteImage.setOnClickListener(view -> {

                images.remove(holder.getAdapterPosition() - 1);
                notifyItemRemoved(holder.getAdapterPosition());

                onImageDeleteListener.onImageDelete(image);
            });
        }


    }

    @Override
    public int getItemCount() {
        return images.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {

        if (position == 0){
            return ADD_IMAGE_VIEW_TYPE;
        }else{
            return IMAGE_VIEW_TYPE;
        }
    }

    // Set default background (when current background deleted)
    public void setDefaultBackground(){
        currentBackground = "R.drawable.back";
        notifyItemChanged(images.indexOf(currentBackground));
    }

    // Setters for listeners
    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public void setOnImageDeleteListener(OnImageDeleteListener onImageDeleteListener) {
        this.onImageDeleteListener = onImageDeleteListener;
    }

    // Setters for images
    public void setCurrentBackground(String currentBackground) {
        this.currentBackground = currentBackground;
    }

    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }
}