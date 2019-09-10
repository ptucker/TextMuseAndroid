package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class NoRegisterDataDialogFragment extends DialogFragment {

    public static NoRegisterDataDialogFragment newInstance() {
        return new NoRegisterDataDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Please complete the information first before submitting.")
                .setTitle("Incomplete Registration Information")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
