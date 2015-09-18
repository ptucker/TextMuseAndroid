package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.Category;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseContact;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseRecentContact;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;

import java.util.ArrayList;


public class SettingsActivity extends ActionBarActivity {

    public static final String SHOWN_CATEGORIES_CHANGED_EXTRA = "com.laloosh.textmuse.settings.categorieschanged";
    private static int REORDER_CATEGORIES_REQUEST_CODE = 9999;

    private ListView mListView;
    private SettingsListAdapter mAdapter;
    private TextMuseSettings mSettings;
    private TextMuseData mData;
    private TextMuseStoredContacts mStoredContacts;

    private boolean mShouldShowCategories;
    private boolean mChangedCategories;

    private ActionMode mActionMode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        GlobalData globalData = GlobalData.getInstance();
        if (!globalData.hasLoadedData()) {
            globalData.loadData(this);
        }

        mSettings = globalData.getSettings();
        if (mSettings == null) {
            mSettings = new TextMuseSettings();
            globalData.updateTextMuseSettings(mSettings);
        }

        mData = globalData.getData();
        if (mData == null || mData.categories == null || mData.categories.size() <= 0) {
            mShouldShowCategories = false;
        } else {
            mShouldShowCategories = true;
        }

        //Refresh the category order using the textmuse data
        mSettings.syncCategoryOrderFromData(mData.categories);
        mSettings.save(this);

        mStoredContacts = globalData.getStoredContacts();
        mChangedCategories = false;

        mAdapter = new SettingsListAdapter();
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {

            setActivityResultValue();
            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        setActivityResultValue();

        super.onBackPressed();
    }

    private void setActivityResultValue() {

        GlobalData.getInstance().updateTextMuseSettings(mSettings);

        if (mChangedCategories) {
            Intent intent = new Intent();
            intent.putExtra(SHOWN_CATEGORIES_CHANGED_EXTRA, true);
            setResult(Activity.RESULT_OK, intent);
        } else {
            setResult(Activity.RESULT_OK);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REORDER_CATEGORIES_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mChangedCategories = true;

            //The order of the categories might have changed.  Let's take this list and reorder our data categories
            mData.reorderCategories(mSettings.getCategoryOrder());
            mData.save(this);

            mAdapter.populateCategoryList();
            mAdapter.notifyDataSetChanged();
        }
    }

    private void startActionMode() {
        if (mActionMode == null) {
            mActionMode = this.startSupportActionMode(mActionModeCallback);
        }
    }

    private void clearActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    protected ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_reorder_category_popup, menu);
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
                case R.id.menu_reorder_category:
                    mActionMode.finish();

                    Intent intent = new Intent(SettingsActivity.this, SortableCategoryActivity.class);
                    startActivityForResult(intent, REORDER_CATEGORIES_REQUEST_CODE);

                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };


    public class SettingsListAdapter extends BaseAdapter {

        private ArrayList<String> mCategoryList;
        private LayoutInflater mLayoutInflater;

        public SettingsListAdapter() {
            if (mShouldShowCategories) {
                populateCategoryList();
            }

            mLayoutInflater = SettingsActivity.this.getLayoutInflater();
        }

        public void populateCategoryList() {
            mCategoryList = mSettings.getCategoryOrder();
        }

        @Override
        public int getCount() {

            //There are a base of 7 (notifications, recent contacts, feedback, register, privacy policy, walkthrough, skins) items
            int total = 7;

            if (mShouldShowCategories) {
                total += mCategoryList.size() + 1;  //category list plus header
            }

            return total;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (position) {
                case 0:
                    return getNotificationView(convertView, parent);
                case 1:
                    return getRecentContactsView(convertView, parent);
                case 2:
                    return getTextOnlyView(convertView, parent, TextOnlyViewType.FEEDBACK_VIEW);
                case 3:
                    return getTextOnlyView(convertView, parent, TextOnlyViewType.REGISTER_VIEW);
                case 4:
                    return getTextOnlyView(convertView, parent, TextOnlyViewType.PRIVACY_POLICY_VIEW);
                case 5:
                    return getTextOnlyView(convertView, parent, TextOnlyViewType.WALKTHROUGH_VIEW);
                case 6:
                    return getTextOnlyView(convertView, parent, TextOnlyViewType.SKIN_VIEW);
                case 7:
                    return getCategoryHeader(convertView, parent);
                default:
                    int adjustedPosition = position - (getCount() - mCategoryList.size());
                    return getCategoryElementView(adjustedPosition, convertView, parent);
            }
        }

        //Don't bother with view holders here, since there's only 1 of these
        private View getNotificationView(View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.settings_notification_ele, parent, false);
            }

            Switch notificationSwitch = (Switch) rowView.findViewById(R.id.settingsSwitchNotification);
            notificationSwitch.setChecked(mSettings.notifications);

            notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSettings.notifications = isChecked;
                    mSettings.save(SettingsActivity.this);
                }
            });

            return rowView;
        }

        //Don't bother with view holders here, since there's only one
        private View getRecentContactsView(View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.settings_recent_contacts_ele, parent, false);
            }

            final SeekBar seekBar = (SeekBar) rowView.findViewById(R.id.settingsSeekBarRecentContacts);
            Switch saveRecentContactsSwitch = (Switch) rowView.findViewById(R.id.settingsSwitchSaveRecentContacts);
            saveRecentContactsSwitch.setChecked(mSettings.saveRecentContacts);

            if (mSettings.saveRecentContacts) {
                seekBar.setEnabled(true);
            } else {
                seekBar.setEnabled(false);
            }

            seekBar.setProgress(mSettings.recentContactLimit - 1);

            saveRecentContactsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mSettings.saveRecentContacts = isChecked;
                    if (isChecked) {
                        seekBar.setEnabled(true);
                    } else {
                        seekBar.setEnabled(false);
                    }
                    mSettings.save(SettingsActivity.this);

                    if (mStoredContacts != null) {
                        mStoredContacts.updateRecentContactsFromSettings(mSettings);
                        mStoredContacts.save(SettingsActivity.this);
                    }
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //Since the seekbar goes from 0 to 9 and we want 1 through 10, just add 1
                    int realValue = seekBar.getProgress() + 1;
                    mSettings.recentContactLimit = realValue;
                    mSettings.save(SettingsActivity.this);
                    Log.d(Constants.TAG, "Saving value of contacts");

                    if (mStoredContacts != null) {
                        mStoredContacts.updateRecentContactsFromSettings(mSettings);
                        mStoredContacts.save(SettingsActivity.this);
                    }
                }
            });

            return rowView;

        }

        //These can be reused, so tag them
        private View getTextOnlyView(View convertView, ViewGroup parent, final TextOnlyViewType viewType) {
            View rowView = convertView;

            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.settings_textitem_ele, parent, false);

                TextItemViewHolder viewHolder = new TextItemViewHolder();
                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.settingsTextViewAction);
                rowView.setTag(viewHolder);
            }

            TextItemViewHolder viewHolder = (TextItemViewHolder) rowView.getTag();

            switch (viewType) {
                case FEEDBACK_VIEW:
                    viewHolder.mTextView.setText("Feedback");
                    break;
                case REGISTER_VIEW:
                    viewHolder.mTextView.setText("Register");
                    break;
                case PRIVACY_POLICY_VIEW:
                    viewHolder.mTextView.setText("Privacy Policy");
                    break;
                case WALKTHROUGH_VIEW:
                    viewHolder.mTextView.setText("Walkthrough");
                    break;
                case SKIN_VIEW:
                    viewHolder.mTextView.setText("Skins");
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewType == TextOnlyViewType.FEEDBACK_VIEW) {

                        Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
                        startActivity(intent);

                    } else if (viewType == TextOnlyViewType.PRIVACY_POLICY_VIEW) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.textmuse.com/privacy-policy"));
                        startActivity(intent);

                    } else if (viewType == TextOnlyViewType.REGISTER_VIEW) {

                        Intent intent = new Intent(SettingsActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    } else if (viewType == TextOnlyViewType.WALKTHROUGH_VIEW) {
                        Intent intent = new Intent(SettingsActivity.this, WalkthroughActivity.class);
                        startActivity(intent);
                    } else if (viewType == TextOnlyViewType.SKIN_VIEW) {
                        Intent intent = new Intent(SettingsActivity.this, SkinSelectActivity.class);
                        startActivity(intent);
                    }
                }
            });

            return rowView;
        }

        private View getCategoryHeader(View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.settings_category_header_ele, parent, false);
            }

            rowView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    startActionMode();
                    return true;
                }
            });

            return rowView;
        }

        private View getCategoryElementView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = mLayoutInflater.inflate(R.layout.settings_category_ele, parent, false);

                CategoryEleViewHolder viewHolder = new CategoryEleViewHolder();
                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.settingsTextViewCategoryName);
                viewHolder.mCheckBox = (CheckBox) rowView.findViewById(R.id.settingsCheckboxCategory);
                rowView.setTag(viewHolder);
            }

            CategoryEleViewHolder viewHolder = (CategoryEleViewHolder) rowView.getTag();
            final String categoryName = mCategoryList.get(position);

            viewHolder.mTextView.setText(categoryName);
            viewHolder.mCheckBox.setOnCheckedChangeListener(null);
            viewHolder.mCheckBox.setChecked(mSettings.shouldShowCategory(categoryName));

            viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(Constants.TAG, "Setting show category for " + categoryName + " to " + isChecked);
                    mSettings.setShowCategory(categoryName, isChecked);
                    mSettings.save(SettingsActivity.this);
                    mChangedCategories = true;
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CategoryEleViewHolder viewHolder = (CategoryEleViewHolder) v.getTag();
                    viewHolder.mCheckBox.toggle();  //this calls the checked change listener
                }
            });

            rowView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    startActionMode();
                    return true;
                }
            });

            return rowView;
        }


        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:         //notifications
                    return 0;
                case 1:         //recent contacts
                    return 1;
                case 2:         //feedback
                    return 2;
                case 3:         //Register
                    return 2;
                case 4:        //privacy policy
                    return 2;
                case 5:        //walkthrough
                    return 2;
                case 6:        //skins
                    return 2;
                case 7:        //Category header
                    return 3;
                default:        //category elements
                    return 4;
            }
        }

        @Override
        public int getViewTypeCount() {
            //5 different types of views
            return 5;
        }

    }

    private enum TextOnlyViewType {
        FEEDBACK_VIEW,
        PRIVACY_POLICY_VIEW,
        REGISTER_VIEW,
        WALKTHROUGH_VIEW,
        SKIN_VIEW
    }

    private class TextItemViewHolder {
        TextView mTextView;
    }

    private class CategoryEleViewHolder {
        TextView mTextView;
        CheckBox mCheckBox;
    }
}
