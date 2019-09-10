package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;

public class NoContactsSelectedDialogFragment extends DialogFragment {

    public static NoContactsSelectedDialogFragment newInstance() {
        return new NoContactsSelectedDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You have not selected any contacts.  Please select some contacts to proceed.")
                .setTitle("No Contacts Selected")
                .setPositiveButton("Ok", null);

        return builder.create();
    }
}
