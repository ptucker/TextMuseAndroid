package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class BadRegisterDataDialogFragment extends DialogFragment {

    public static BadRegisterDataDialogFragment newInstance() {
        return new BadRegisterDataDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please ensure that the information entered is correct before submitting.")
                .setTitle("Invalid Information Entered")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
