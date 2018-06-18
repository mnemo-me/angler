package com.mnemo.angler.background_changer;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.mnemo.angler.MainActivity;
import com.mnemo.angler.R;

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
            /*
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            background.setColorFilter(new ColorMatrixColorFilter(matrix));
            */
        }else{
            holder.cardView.setActivated(false);
            if (image.equals(selectedImage)){
                holder.cardView.setSelected(true);
            }else{
                holder.cardView.setSelected(false);
            }
        }

        ImageAssistent.loadImage(context, image, background, 270);

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
                    onImageClickListener.onImageClick(image);
                }
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
}