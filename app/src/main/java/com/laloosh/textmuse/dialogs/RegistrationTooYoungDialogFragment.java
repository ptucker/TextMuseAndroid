package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class RegistrationTooYoungDialogFragment extends DialogFragment {

    public static RegistrationTooYoungDialogFragment newInstance() {
        return new RegistrationTooYoungDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("TextMuse is not intended for anyone under the age of 13.")
                .setTitle("Cannot Register")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
