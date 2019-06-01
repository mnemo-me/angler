package com.mnemo.angler.ui.main_activity.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.util.ImageAssistant;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.ViewHolder> {

    private Context context;
    private List<String> folders;

    private OnFolderClickListener onFolderClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.folder_image)
        ImageView folderImage;

        @BindView(R.id.folder_name)
        TextView folderName;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnFolderClickListener {
        void onFolderClick(String name);
    }


    public FoldersAdapter(Context context, List<String> folders) {
        this.context = context;
        this.folders = folders;
    }

    @NonNull
    public FoldersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.fold_folder_v2, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        // Setup listeners
        viewHolder.itemView.setOnClickListener(v -> {

            // Get playlist data
            String folder = folders.get(viewHolder.getAdapterPosition());

            onFolderClickListener.onFolderClick(folder);
        });


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FoldersAdapter.ViewHolder holder, int position) {

        // Get playlist data
        String folderName = folders.get(position);

        // Assign data to views
        holder.folderName.setText(new File(folderName).getName());
        ImageAssistant.loadImage(context, "R.drawable.folder", holder.folderImage, 72);

    }

    @Override
    public int getItemCount() {
        return folders.size();
    }


    // Setter for listeners
    public void setOnFolderClickListener(OnFolderClickListener onPlaylistClickListener) {
        this.onFolderClickListener = onPlaylistClickListener;
    }

}
