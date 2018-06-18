package com.mnemo.angler.playlist_manager;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mnemo.angler.R;
import com.mnemo.angler.data.AnglerContract.*;
import com.mnemo.angler.data.AnglerSQLiteDBHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlaylistRenameDialogFragment extends DialogFragment {

    EditText editText;
    TextView errorText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String oldTitle = getArguments().getString("title");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LinearLayout titleLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_dialog_title, null, false);
        TextView title = titleLayout.findViewById(R.id.dialog_title);
        title.setText(R.string.enter_new_playlist_title);
        builder.setCustomTitle(titleLayout);

        LinearLayout bodyLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.pm_rename_dialog, null, false);
        editText = bodyLayout.findViewById(R.id.rename_dialog_edit_text);
        errorText = bodyLayout.findViewById(R.id.rename_dialog_error);
        editText.setHint(oldTitle);
        builder.setView(bodyLayout);

        builder.setPositiveButton(R.string.rename, null);

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });


        return builder.create();
    }


    @Override
    public void onStart() {
        super.onStart();

        // show soft keyboard based on focus
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

                editText.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });


            }
        });

        editText.requestFocus();


        AlertDialog alertDialog = (AlertDialog)getDialog();
        Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newTitle = editText.getText().toString();

                errorText.setText("");

                Cursor playlistNameCheckerCursor = getActivity().getContentResolver().query(PlaylistEntry.CONTENT_URI, null,
                        PlaylistEntry.COLUMN_NAME + " = ? OR " + PlaylistEntry.COLUMN_TRACKS_TABLE + " = ?", new String[]{newTitle, AnglerSQLiteDBHelper.createTrackTableName(newTitle)},
                        null);

                Boolean isPlaylistNameAlreadyExist = playlistNameCheckerCursor.getCount() == 1;
                playlistNameCheckerCursor.close();

                // create regex pattern
                String regex = "^[\\p{L}\\d _!.,:'-]+$";
                Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(newTitle);


                if (SourceEntry.SOURCES.contains(newTitle.replace(" ", "_"))
                        || SourceEntry.SOURCES.contains(newTitle.substring(0,1).toUpperCase() + newTitle.replace(" ", "_").substring(1))
                        || newTitle.equals(getResources().getString(R.string.new_playlist))) {
                    errorText.setText(R.string.playlist_name_reserved);
                }else if (isPlaylistNameAlreadyExist) {
                    errorText.setText(R.string.playlist_name_in_used);
                }else if (newTitle.length() > 16) {
                    errorText.setText(R.string.too_many_symbols_in_playlist_name);
                }else if(!matcher.find()){
                    errorText.setText(R.string.incorrect_playlist_name);
                }else {

                    if (!TextUtils.isEmpty(newTitle)) {
                        ((PlaylistOptionsFragment) getActivity().getSupportFragmentManager().findFragmentByTag("playlist_opt_fragment")).changeTitle(newTitle);
                    }
                    dismiss();
                }
            }
        });
    }
}
