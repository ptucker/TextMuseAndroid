package com.laloosh.textmuse.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.laloosh.textmuse.Constants;


public class NoUsersDialogFragment extends DialogFragment {
    private NoUsersDialogHandler mHandler;

    public static NoUsersDialogFragment newInstance() {
        return new NoUsersDialogFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        try {
            mHandler = (NoUsersDialogHandler) activity;
        } catch (ClassCastException e) {
            Log.e(Constants.TAG, "Could not get callback for no users dialog fragment");
            mHandler = null;
        }

        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mHandler = null;

        super.onDetach();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("This group contains no contacts.  If you proceed, this group will be removed.")
                .setTitle("No Contacts")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mHandler != null) {
                            mHandler.noUsersProceedToDelete();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mHandler != null) {
                            mHandler.noUsersCancel();
                        }
                    }
                })
        ;

        return builder.create();
    }

    public interface NoUsersDialogHandler {
        public void noUsersProceedToDelete();
        public void noUsersCancel();
    }
}
