package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SetHighlightProblemDialogFragment extends DialogFragment {

    public static SetHighlightProblemDialogFragment newInstance() {
        return new SetHighlightProblemDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("There was a connection problem when attempting to highlight this text.  Please try again later.")
                .setTitle("Could Not Highlight Text")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
