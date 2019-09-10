package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class GeneralDialogFragment extends DialogFragment {
    private static final String ARG_TITLE = "title" ;
    private static final String ARG_MESSAGE = "message";

    private String mTitle;
    private String mMessage;

    public static GeneralDialogFragment newInstance(String title, String message) {
        GeneralDialogFragment fragment = new GeneralDialogFragment();

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
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
