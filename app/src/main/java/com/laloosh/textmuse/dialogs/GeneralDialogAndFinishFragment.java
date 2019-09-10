package com.laloosh.textmuse.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class GeneralDialogAndFinishFragment extends DialogFragment {
    private static final String ARG_TITLE = "title" ;
    private static final String ARG_MESSAGE = "message";

    private String mTitle;
    private String mMessage;

    public static GeneralDialogAndFinishFragment newInstance(String title, String message) {
        GeneralDialogAndFinishFragment fragment = new GeneralDialogAndFinishFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(ARG_TITLE);
            mMessage = getArguments().getString(ARG_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setTitle(mTitle)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Activity activity = getActivity();
                        if (activity != null) {
                            activity.finish();
                        }

                        dismiss();
                    }
                });

        return builder.create();
    }
}
