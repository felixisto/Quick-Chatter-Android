package com.office.quickchatter.ui.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.office.quickchatter.presenter.BasePresenter;
import com.office.quickchatter.utilities.Callback;

public class FileDestinationDialogHandler implements BasePresenter.FileDestinationHandler {
    private @Nullable Fragment _fragment;

    public void setFragment(@Nullable Fragment fragment) {
        _fragment = fragment;
    }

    // # Presenter.FileDestinationHandler

    @Override
    public void onFileOverwrite(final @NonNull Callback<Boolean> handler) {
        if (_fragment == null) {
            return;
        }

        final Context context = _fragment.getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("File name");
        builder.setMessage("Overwrite file?");
        builder.setCancelable(true);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.perform(false);
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.perform(true);
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onPickFileName(@NonNull final Callback<String> handler, @NonNull String initialName) {
        if (_fragment == null) {
            return;
        }

        final Context context = _fragment.getContext();
        final EditText editFileNameField = getEditFileName(context);

        editFileNameField.setText(initialName);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("File name");
        builder.setCancelable(true);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.perform("");
                dialog.cancel();
            }
        });

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.perform(editFileNameField.getText().toString());
            }
        });

        AlertDialog alert = builder.create();

        alert.setView(editFileNameField);

        alert.show();
    }

    // # Internals

    private @NonNull EditText getEditFileName(@NonNull Context context) {
        final EditText fieldFileName = new EditText(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        fieldFileName.setLayoutParams(lp);
        return fieldFileName;
    }
}
