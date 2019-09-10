package com.laloosh.textmuse.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.R;


public class EnterGroupDialogFragment extends DialogFragment {

    private static final String NAME_ARG_KEY = "com.laloosh.textmuse.entergroupdialogfragment.name";

    private GroupNameChangeHandler mHandler = null;

    public static EnterGroupDialogFragment newInstance() {

        return new EnterGroupDialogFragment();
    }

    public static EnterGroupDialogFragment newInstance(String name) {

        if (name == null || name.isEmpty()) {
            return newInstance();
        }

        EnterGroupDialogFragment fragment = new EnterGroupDialogFragment();

        Bundle args = new Bundle();
        args.putString(NAME_ARG_KEY, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            Fragment fragment = getTargetFragment();
            if (fragment != null) {
                mHandler = (GroupNameChangeHandler) fragment;
            } else {
                mHandler = (GroupNameChangeHandler) activity;
            }

        } catch (ClassCastException e) {
            Log.e(Constants.TAG, "Enter group name called from unsupported activity or fragment!");
            mHandler = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHandler = null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mHandler != null) {
            mHandler.onGroupNameEditCancel();
        }
        super.onCancel(dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String originalName;
        if (getArguments() != null) {
            originalName = getArguments().getString(NAME_ARG_KEY, null);
        } else {
            originalName = null;
        }

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_group_name, null);

        final TextView groupNameInstructions = (TextView) view.findViewById(R.id.dialogGroupNameTextViewDescription);
        final EditText groupName = (EditText) view.findViewById(R.id.dialogGroupNameEditText);

        if (originalName != null && !originalName.isEmpty()) {
            groupName.setText(originalName);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("New Group")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mHandler != null) {
                            mHandler.onGroupNameEditCancel();
                        }
                    }
                });

        final AlertDialog d = builder.create();

        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String groupNameString = groupName.getText().toString();

                        if (groupNameString.isEmpty()) {
                            groupNameInstructions.setText(getString(R.string.enter_group_name_error));
                            groupNameInstructions.setTextColor(0xffcc0000);
                        } else {
                            if (mHandler != null) {

                                boolean isUsableName = mHandler.isUsableGroupName(groupNameString);

                                if (originalName == null) {
                                    //Adding a group
                                   if (isUsableName) {
                                       mHandler.handleNewGroupName(groupNameString);
                                       d.dismiss();
                                   } else {
                                       groupNameInstructions.setText(getString(R.string.enter_group_name_duplicate_error));
                                       groupNameInstructions.setTextColor(0xffcc0000);
                                   }
                                } else {
                                    //Editing a group
                                    if (originalName.equals(groupNameString)) {
                                        //If the user didn't change the name of the group, just do the equivalent of a cancel
                                        d.dismiss();
                                    } else if (isUsableName) {
                                        mHandler.handleRenameGroupName(originalName, groupNameString);
                                        d.dismiss();
                                    } else {
                                        groupNameInstructions.setText(getString(R.string.enter_group_name_duplicate_error));
                                        groupNameInstructions.setTextColor(0xffcc0000);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });

        return d;
    }

    public interface GroupNameChangeHandler {
        public void handleNewGroupName(String name);
        public void handleRenameGroupName(String oldname, String newName);
        public boolean isUsableGroupName(String name);
        public void onGroupNameEditCancel();
    }
}

