package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class CannotSendTextDialogFragment extends DialogFragment {

    public static CannotSendTextDialogFragment newInstance() {
        return new CannotSendTextDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("TextMuse was unable to send this text. Make sure that your phone has SMS and MMS support.")
                .setTitle("Cannot Send Text")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
