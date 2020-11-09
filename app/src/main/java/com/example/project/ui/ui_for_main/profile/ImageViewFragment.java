package com.example.project.ui.ui_for_main.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.project.R;

public class ImageViewFragment extends AppCompatDialogFragment {
    private Bitmap bitmap;

    public ImageViewFragment(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_image_view, null);

        ImageButton closeBtn = v.findViewById(R.id.closeButtonImageView);
        closeBtn.setOnClickListener(v1 -> {
            dismiss();
        });

        ImageView icon = v.findViewById(R.id.iconImageView);
        icon.setImageBitmap(bitmap);

        Dialog dialog = new AlertDialog.Builder(getActivity()).setView(v).create();
        dialog.setTitle(null);
        return dialog;
    }
}


