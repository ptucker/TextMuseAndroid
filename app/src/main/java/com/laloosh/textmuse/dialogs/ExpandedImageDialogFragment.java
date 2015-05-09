package com.laloosh.textmuse.dialogs;


import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.laloosh.textmuse.Constants;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.Note;
import com.squareup.picasso.Picasso;

public class ExpandedImageDialogFragment extends DialogFragment implements ImageSizeDownloadTarget.ImageSizeDownloadTargetHandler {

    private static final float DIALOG_MAX_SIZE = 0.9f;
    private static final String NOTE_ARG_KEY = "note";

    private int mDialogWidth = -1;
    private int mDialogHeight = -1;

    private ImageView mImageView;

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

        mImageView = (ImageView) view.findViewById(R.id.dialogExpandedImageView);

        Log.d(Constants.TAG, "Attempting to load media url: " + note.mediaUrl);

        ImageSizeDownloadTarget target = new ImageSizeDownloadTarget(this);

        Picasso.with(getActivity())
                .load(note.mediaUrl)
                .into(target);

        return view;

    }

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

        Log.d(Constants.TAG, "Done loading bitmap, will scale to height " + mDialogHeight + " and width " + mDialogWidth);

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(mDialogWidth, mDialogHeight);
        }

        mImageView.setImageBitmap(bitmap);
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

}
