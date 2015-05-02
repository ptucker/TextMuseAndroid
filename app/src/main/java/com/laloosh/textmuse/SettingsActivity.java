package com.laloosh.textmuse;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
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
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;
import com.laloosh.textmuse.datamodel.TextMuseStoredContacts;

import java.util.ArrayList;


public class SettingsActivity extends ActionBarActivity {

    private ListView mListView;
    private SettingsListAdapter mAdapter;
    private TextMuseSettings mSettings;
    private TextMuseData mData;

    private boolean mShouldShowCategories;

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

        mAdapter = new SettingsListAdapter();
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

    }

    public class SettingsListAdapter extends BaseAdapter implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{

        private ArrayList<String> mCategoryList;
        private LayoutInflater mLayoutInflater;

        public SettingsListAdapter() {
            if (mShouldShowCategories) {
                mCategoryList = new ArrayList<String> ();
                for (Category c : mData.categories) {
                    mCategoryList.add(c.name);
                }
            }

            mLayoutInflater = SettingsActivity.this.getLayoutInflater();
        }

        @Override
        public int getCount() {

            //There are a base of 4 (notifications, recent contacts, feedback, privacy policy) items
            int total = 4;

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
                    return getTextOnlyView(convertView, parent, TextOnlyViewType.PRIVACY_POLICY_VIEW);
                case 4:
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
                case PRIVACY_POLICY_VIEW:
                    viewHolder.mTextView.setText("Privacy Policy");
                    break;
            }

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewType == TextOnlyViewType.FEEDBACK_VIEW) {

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri data = Uri.parse("mailto:info@textmuse.com?subject=Feedback for TextMuse");
                        intent.setData(data);
                        startActivity(intent);

                    } else if (viewType == TextOnlyViewType.PRIVACY_POLICY_VIEW) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.textmuse.com/privacy-policy"));
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
            viewHolder.mCheckBox.setChecked(mSettings.shouldShowCategory(categoryName));

            viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(Constants.TAG, "Setting show category for " + categoryName + " to " + isChecked);
                    mSettings.setShowCategory(categoryName, isChecked);
                    mSettings.save(SettingsActivity.this);
                }
            });

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CategoryEleViewHolder viewHolder = (CategoryEleViewHolder) v.getTag();
                    viewHolder.mCheckBox.toggle();  //this calls the checked change listener
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
                case 3:         //privacy policy
                    return 2;
                case 4:         //category header
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

        @Override
        public void onClick(View v) {

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        }



    }

    private enum TextOnlyViewType {
        FEEDBACK_VIEW,
        PRIVACY_POLICY_VIEW
    }

    private class TextItemViewHolder {
        TextView mTextView;
    }

    private class CategoryEleViewHolder {
        TextView mTextView;
        CheckBox mCheckBox;
    }
}
