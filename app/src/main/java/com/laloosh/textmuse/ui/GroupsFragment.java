package com.laloosh.textmuse.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseGroup;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;
import com.laloosh.textmuse.dialogs.EnterGroupDialogFragment;

import java.util.List;

import de.greenrobot.event.EventBus;


public class GroupsFragment extends Fragment implements EnterGroupDialogFragment.GroupNameChangeHandler {

    private static final int REQUEST_CODE_GROUPEDIT = 1234;
    private static final int REQUEST_CODE_DIALOG = 1111;

    private ListView mListView;
    private GroupsAdapter mAdapter;

    private ActionMode mActionMode = null;
    private int mActionModeIndex = -1;
    private TextMuseStoredContacts mStoredContacts;

    public GroupsFragment() {
        // Required empty public constructor
    }

    public static GroupsFragment newInstance() {
        GroupsFragment fragment = new GroupsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_groups, container, false);

        mListView = (ListView) v.findViewById(R.id.groupsFragmentListView);

        mStoredContacts = GlobalData.getInstance().getStoredContacts();
        mAdapter = new GroupsAdapter(this, mStoredContacts.groups);

        mListView.setAdapter(mAdapter);

        return v;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_add_group) {
            createNewGroup();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mAdapter != null) {
            TextMuseStoredContacts contacts = GlobalData.getInstance().getStoredContacts();
            mStoredContacts = contacts;
            mAdapter.updateGroups(mStoredContacts.groups);
        }
    }

    //This is used by eventbus once we get the event
    public void onEvent(GroupActionEvent event) {
        mActionModeIndex = event.getIndex();
        startActionMode();
    }

    private void startActionMode() {
        if (mActionMode == null) {
            HomeActivity activity = (HomeActivity) getActivity();
            mActionMode = activity.startSupportActionMode(mActionModeCallback);
        }
    }

    private void clearActionMode() {
        if (mActionMode != null) {
            mActionModeIndex = -1;
            mActionMode.finish();
        }
    }

    protected ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_groups_popup, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_remove_group:

                    if (mActionModeIndex < 0) {
                        mActionMode.finish();
                    } else {
                        removeGroup();
                        clearActionMode();
                    }

                    return true;

                case R.id.menu_rename_group:
                    if (mActionModeIndex < 0) {
                        mActionMode.finish();
                    } else {
                        renameGroup();
                        mActionMode.finish();
                    }

                    return true;

                case R.id.menu_edit_group:
                    if (mActionModeIndex < 0) {
                        mActionMode.finish();
                    } else {
                        editGroup();
                    }

                    return true;

                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mActionModeIndex = -1;
            mAdapter.notifyDataSetChanged();
        }
    };

    //Called when we start adding a new group, either through the menu or through the tile
    public void createNewGroup() {
        Log.d(Constants.TAG, "Launching add a group flow");
        EnterGroupDialogFragment fragment = EnterGroupDialogFragment.newInstance();
        fragment.setTargetFragment(this, REQUEST_CODE_DIALOG);
        fragment.show(getFragmentManager(), "enterGroupFragment");
    }

    //renames the group with index mActionModeIndex
    public void renameGroup() {
        Log.d(Constants.TAG, "Renaming group");
        TextMuseGroup group = mStoredContacts.groups.get(mActionModeIndex);
        EnterGroupDialogFragment fragment = EnterGroupDialogFragment.newInstance(group.displayName);
        fragment.setTargetFragment(this, REQUEST_CODE_DIALOG);
        fragment.show(getFragmentManager(), "enterGroupFragment");
    }

    public void removeGroup() {
        Log.d(Constants.TAG, "Removing group");
        TextMuseGroup group = mStoredContacts.groups.get(mActionModeIndex);
        mStoredContacts.removeGroup(group);
        mStoredContacts.save(getContext());
        mAdapter.updateGroups(mStoredContacts.groups);
    }

    public void editGroup() {
        Log.d(Constants.TAG, "Editing group");

        TextMuseGroup group = mStoredContacts.groups.get(mActionModeIndex);
        Intent intent = new Intent(getContext(), GroupEditActivity.class);
        intent.putExtra(GroupEditActivity.EXISTING_GROUP, group.displayName);

        clearActionMode();

        startActivityForResult(intent, REQUEST_CODE_GROUPEDIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GROUPEDIT) {
            TextMuseStoredContacts contacts = GlobalData.getInstance().getStoredContacts();
            mStoredContacts = contacts;
            mAdapter.updateGroups(mStoredContacts.groups);
        }
    }


    @Override
    public void handleNewGroupName(String name) {
        if (name != null && !name.isEmpty()) {
            Intent intent = new Intent(getContext(), GroupEditActivity.class);
            intent.putExtra(GroupEditActivity.NEW_GROUP_NAME_EXTRA, name);
            startActivityForResult(intent, REQUEST_CODE_GROUPEDIT);
        }
    }

    @Override
    public void handleRenameGroupName(String oldname, String newName) {
        TextMuseGroup group = mStoredContacts.getGroup(oldname);
        if (group != null) {
            group.displayName = newName;
        }

        mStoredContacts.save(getContext());
        mAdapter.updateGroups(mStoredContacts.groups);
        clearActionMode();
    }

    @Override
    public void onGroupNameEditCancel() {
        clearActionMode();
    }

    @Override
    public boolean isUsableGroupName(String name) {
        return !(mStoredContacts.groupNameExists(name));
    }


    private static class GroupsAdapter extends ArrayAdapter<TextMuseGroup> {
        private Fragment mFragment;
        private List<TextMuseGroup> mGroups;

        public static class ViewHolder {
            public TextView mTextView;
            public TextView mTextViewCount;
        }

        public GroupsAdapter(Fragment fragment, List<TextMuseGroup> groups) {
            super(fragment.getContext(), R.layout.groups_list_ele_name, groups);

            mFragment = fragment;
            mGroups = groups;
        }

        @Override
        public int getCount() {
            return mGroups.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            final TextMuseGroup group = mGroups.get(position);

            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(mFragment.getContext());
                ViewHolder viewHolder = new ViewHolder();

                rowView = inflater.inflate(R.layout.groups_list_ele_name, parent, false);
                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.groupListEleTextViewName);
                viewHolder.mTextViewCount = (TextView) rowView.findViewById(R.id.groupListEleTextViewCount);

                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();

            holder.mTextView.setText(group.displayName);
            holder.mTextViewCount.setText(Integer.toString(group.contactList.size()));

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(Constants.TAG, "Row clicked");

                    Intent intent = new Intent(getContext(), GroupEditActivity.class);
                    intent.putExtra(GroupEditActivity.EXISTING_GROUP, group.displayName);
                    mFragment.startActivityForResult(intent, REQUEST_CODE_GROUPEDIT);
                }
            });

            rowView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    EventBus.getDefault().post(new GroupActionEvent(position));
                    return true;
                }
            });

            return rowView;
        }

        public void updateGroups(List<TextMuseGroup> groups) {
            mGroups = groups;
            notifyDataSetChanged();
        }
    }

    public static class GroupActionEvent {
        private int mActionModeIndex;

        public GroupActionEvent(int index) {
            mActionModeIndex = index;
        }

        public int getIndex() {
            return mActionModeIndex;
        }
    }

}
