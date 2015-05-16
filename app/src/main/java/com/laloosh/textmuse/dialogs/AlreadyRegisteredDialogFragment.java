package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlreadyRegisteredDialogFragment extends DialogFragment {

    public static AlreadyRegisteredDialogFragment newInstance() {
        return new AlreadyRegisteredDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You have already registered. Thank you for registering!")
                .setTitle("No Need To Register")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
