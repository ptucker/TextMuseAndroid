package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LaunchMessageDialogFragment extends DialogFragment {

    private static final String MESSAGE_BUNDLE_KEY = "launchMessage";

    public static LaunchMessageDialogFragment newInstance(String message) {
        LaunchMessageDialogFragment fragment = new LaunchMessageDialogFragment();

        Bundle args = new Bundle();
        args.putString(MESSAGE_BUNDLE_KEY, message);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String message;
        if (getArguments() != null) {
            message = getArguments().getString(MESSAGE_BUNDLE_KEY, null);
        } else {
            message = "Welcome to TextMuse!";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle("TextMuse")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
