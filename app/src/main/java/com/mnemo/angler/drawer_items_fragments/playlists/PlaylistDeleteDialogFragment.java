package com.mnemo.angler.drawer_items_fragments.playlists;



import android.app.AlertDialog;
import android.app.Dialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.mnemo.angler.data.database.AnglerSQLiteDBHelper;
import com.mnemo.angler.R;
import com.mnemo.angler.data.database.AnglerContract.*;
import com.mnemo.angler.data.file_storage.AnglerFolder;

import java.io.File;


public class PlaylistDeleteDialogFragment extends DialogFragment {

    SQLiteDatabase db;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String title = getArguments().getString("title");
        final String dbName = getArguments().getString("db_name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.delete_playlist_answer)
                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {

                    // Deleting playlist from database with cover image
                    AnglerSQLiteDBHelper dbHelper = new AnglerSQLiteDBHelper(getContext());
                    db = dbHelper.getWritableDatabase();

                    db.execSQL("DROP TABLE IF EXISTS " + dbName + ";");

                    getActivity().getContentResolver().delete(PlaylistEntry.CONTENT_URI, PlaylistEntry.COLUMN_NAME + " = ?", new String[]{title});

                    File cover = new File(AnglerFolder.PATH_PLAYLIST_COVER + File.separator + title.replace(" ", "_") + ".jpeg");
                    cover.delete();

                    Toast.makeText(getContext(), "Playlist '" + title + "' deleted", Toast.LENGTH_SHORT).show();


                    PlaylistConfigurationFragment playlistConfigurationFragment = (PlaylistConfigurationFragment) getActivity().getSupportFragmentManager().findFragmentByTag("playlist_conf_fragment");

                    if (playlistConfigurationFragment != null){
                        getActivity().onBackPressed();
                    }
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {

                });

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (db != null){
            db.close();
        }
    }

}
