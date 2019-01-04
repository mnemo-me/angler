package com.mnemo.angler.ui.local_load_activity.adapters;

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
import com.mnemo.angler.ui.local_load_activity.fragments.image_carousel.ImageCarouselFragment;
import com.mnemo.angler.util.ImageAssistant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageFolderAdapter extends RecyclerView.Adapter<ImageFolderAdapter.ViewHolder>{

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.folder_image)
        ImageView imageView;

        ViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }


    private Context context;
    private List<String> images;

    public ImageFolderAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }

    @Override
    @NonNull
    public ImageFolderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ll_folder_image_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        // Setup listener
        viewHolder.itemView.setOnClickListener(v -> {

            String image = images.get(viewHolder.getAdapterPosition());

            ImageCarouselFragment imageCarouselFragment = new ImageCarouselFragment();

            Bundle args = new Bundle();
            args.putStringArrayList("images",new ArrayList<>(images));
            args.putString("image",image);
            args.putInt("position", viewHolder.getAdapterPosition());
            imageCarouselFragment.setArguments(args);

            imageCarouselFragment.setSharedElementEnterTransition(new Fade());

            ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction()
                    .addSharedElement(viewHolder.imageView, context.getResources().getString(R.string.local_load_image_transition) + viewHolder.getAdapterPosition())
                    .add(R.id.full_frame, imageCarouselFragment)
                    .addToBackStack(null)
                    .commit();

        });

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        String image = images.get(holder.getAdapterPosition());

        holder.imageView.setTransitionName(context.getResources().getString(R.string.local_load_image_transition) + holder.getAdapterPosition());

        ImageAssistant.loadImage(context, image, holder.imageView, 135);

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}