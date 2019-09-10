package com.laloosh.textmuse.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.TextMuseContact;

import java.util.ArrayList;
import java.util.List;


public class ChoosePhoneNumberDialogFragment extends DialogFragment{

    private static final String CONTACTS_ARG_KEY = "contacts";
    private PhoneChoiceAdapter mAdapter;
    private ChoosePhoneNumberDialogHandler mHandler;

    public static ChoosePhoneNumberDialogFragment newInstance(ArrayList<TextMuseContact> contacts) {
        ChoosePhoneNumberDialogFragment fragment= new ChoosePhoneNumberDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(CONTACTS_ARG_KEY, contacts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ArrayList<TextMuseContact> contacts = getArguments().getParcelableArrayList(CONTACTS_ARG_KEY);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_choose_phonenum, null);

        ListView listView = (ListView) view.findViewById(android.R.id.list);
        mAdapter = new PhoneChoiceAdapter(getActivity(), contacts);
        listView.setAdapter(mAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("Choose phone number")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mHandler != null) {
                            int index = mAdapter.getSelectedIndex();
                            mHandler.selectedContact(contacts.get(index));
                        }
                        ChoosePhoneNumberDialogFragment.this.dismiss();
                    }
                });

        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mHandler != null) {
            mHandler.canceledContactSelection();
        }
    }

    public void setHandler(ChoosePhoneNumberDialogHandler handler) {
        mHandler = handler;
    }

    public class PhoneChoiceAdapter extends ArrayAdapter<TextMuseContact> {

        private LayoutInflater mLayoutInflater;
        private Context mContext;
        private List<TextMuseContact> mContacts;
        private int mSelectedIndex = 0;

        private class PhoneChoiceViewHolder {
            public TextView numberType;
            public TextView phoneNumber;
            public RadioButton radioButton;
        }

        public PhoneChoiceAdapter(Context context, List<TextMuseContact> objects) {
            super(context, R.layout.dialog_choose_phonenum_list_ele, objects);
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mContacts = objects;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.dialog_choose_phonenum_list_ele, parent, false);

                PhoneChoiceViewHolder viewHolder = new PhoneChoiceViewHolder();
                viewHolder.numberType = (TextView) rowView.findViewById(R.id.dialogChoosePhoneListItemNumberType);
                viewHolder.phoneNumber = (TextView) rowView.findViewById(R.id.dialogChoosePhoneListItemNumber);
                viewHolder.radioButton = (RadioButton) rowView.findViewById(R.id.dialogChoosePhoneListRadioButton);
                rowView.setTag(viewHolder);
            }

            PhoneChoiceViewHolder viewHolder = (PhoneChoiceViewHolder) rowView.getTag();
            TextMuseContact contact = mContacts.get(position);
            viewHolder.numberType.setText(contact.numberType + ":");
            viewHolder.phoneNumber.setText(contact.phoneNumber);

            viewHolder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != mSelectedIndex) {
                        mSelectedIndex = position;
                        notifyDataSetChanged();
                    }
                }
            });

            if (mSelectedIndex != position){
                viewHolder.radioButton.setChecked(false);
            } else {
                viewHolder.radioButton.setChecked(true);
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != mSelectedIndex) {
                        mSelectedIndex = position;
                        notifyDataSetChanged();
                    }
                }
            });

            return rowView;
        }

        public int getSelectedIndex() {
            return mSelectedIndex;
        }
    }

    public interface ChoosePhoneNumberDialogHandler {
        public void selectedContact(TextMuseContact contact);
        public void canceledContactSelection();
    }
}

