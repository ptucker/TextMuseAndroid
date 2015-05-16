package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class NoFeedbackDialogFragment extends DialogFragment {

    public static NoFeedbackDialogFragment newInstance() {
        return new NoFeedbackDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please enter some information first before sending feedback.")
                .setTitle("No Feedback Entered")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
