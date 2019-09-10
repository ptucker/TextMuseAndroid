package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class PhoneNumberRemovedDialogFragment extends DialogFragment {

    public static PhoneNumberRemovedDialogFragment newInstance() {
        return new PhoneNumberRemovedDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("We could not get the phone number for this contact.  Please pick a different contact.")
                .setTitle("Could not get phone number")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PhoneNumberRemovedDialogFragment.this.dismiss();
                    }
                });

        return builder.create();
    }
}
