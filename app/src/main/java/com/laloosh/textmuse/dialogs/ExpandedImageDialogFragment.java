package com.laloosh.textmuse.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.laloosh.textmuse.Constants;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.Note;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.lang.ref.WeakReference;

public class ExpandedImageDialogFragment extends DialogFragment{

    private static final float DIALOG_MAX_SIZE = 0.9f;
    private static final String NOTE_ARG_KEY = "note";

    private int mDialogWidth = -1;
    private int mDialogHeight = -1;

    public static ExpandedImageDialogFragment newInstance(Note note) {
        if (note == null || !note.hasDisplayableMedia()) {
            return null;
        }

        ExpandedImageDialogFragment fragment = new ExpandedImageDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(NOTE_ARG_KEY, note);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        Note note = args.getParcelable(NOTE_ARG_KEY);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_expanded_image, null);

        final ImageView imageView = (ImageView) view.findViewById(R.id.dialogExpandedImageView);

        Log.d(Constants.TAG, "Attempting to load media url: " + note.mediaUrl);

        ImageSizeDownloadTarget.ImageSizeDownloadTargetHandler handler = new ImageSizeDownloadTarget.ImageSizeDownloadTargetHandler() {

            //Do some calculations to resize the dialog for the image to fit in the best manner
            @Override
            public void doneLoadingBitmap(Bitmap bitmap) {
                int imgWidth = bitmap.getWidth();
                int imgHeight = bitmap.getHeight();

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point screenSize = new Point();
                display.getSize(screenSize);

                //These are floats so we can do some math with them; we'll round them later
                float dialogWidth, dialogHeight;
                float screenMaxWidth, screenMaxHeight;

                screenMaxWidth = (float)screenSize.x * DIALOG_MAX_SIZE;
                screenMaxHeight = (float)screenSize.y * DIALOG_MAX_SIZE;

                if (imgWidth >= imgHeight) {

                    //width is the limiting factor

                    dialogWidth = screenMaxWidth;
                    dialogHeight = (float) imgHeight / ((float) imgWidth / dialogWidth);

                } else {
                    dialogHeight = screenMaxHeight;
                    dialogWidth = (float) imgWidth / ((float) imgHeight / dialogHeight);
                }

                if (dialogWidth > screenMaxWidth) {
                    float multiplier = screenMaxWidth / dialogWidth;
                    dialogWidth = screenMaxWidth;
                    dialogHeight *= multiplier;
                }

                if (dialogHeight > screenMaxHeight) {
                    float multiplier = screenMaxHeight / dialogHeight;
                    dialogHeight = screenMaxHeight;
                    dialogWidth *= multiplier;
                }

                mDialogHeight = (int) dialogHeight;
                mDialogWidth = (int) dialogWidth;

                Dialog dialog = getDialog();
                if (dialog != null) {
                    dialog.getWindow().setLayout(mDialogWidth, mDialogHeight);
                }

                imageView.setImageBitmap(bitmap);
            }
        };

        ImageSizeDownloadTarget target = new ImageSizeDownloadTarget(handler);

        Picasso.with(getActivity())
                .load(note.mediaUrl)
                .into(target);

        return view;

    }

     @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mDialogWidth > 0 && mDialogHeight > 0) {
            getDialog().getWindow().setLayout(mDialogWidth, mDialogHeight);
        }
    }


    public static class ImageSizeDownloadTarget implements Target {

        ImageSizeDownloadTargetHandler mHandler;

        public ImageSizeDownloadTarget(ImageSizeDownloadTargetHandler handler) {
            mHandler = handler;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mHandler != null) {
                mHandler.doneLoadingBitmap(bitmap);
            }
        }


        public interface ImageSizeDownloadTargetHandler {
            public void doneLoadingBitmap(Bitmap bitmap);
        }
    }
}
