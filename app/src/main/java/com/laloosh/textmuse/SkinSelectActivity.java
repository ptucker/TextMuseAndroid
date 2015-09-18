package com.laloosh.textmuse;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSkin;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SkinSelectActivity extends ActionBarActivity implements FetchNotesAsyncTask.FetchNotesAsyncTaskHandler{

    public static final String EXTRA_LAUNCH_FROM_SPLASH = "launch_from_splash";

    ProgressBar mProgressBar;
    ListView mListView;
    SkinSelectListAdapter mAdapter;
    ArrayList<TextMuseSkin> mSkins;
    int mPreviousSkinId;
    int mSelectedSkinIndex;
    boolean mLaunchedFromSplash;
    boolean mLoadingSkinChange;
    boolean mCloseInProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skin_select);

        Intent intent = getIntent();
        mLaunchedFromSplash = intent.getBooleanExtra(EXTRA_LAUNCH_FROM_SPLASH, false);

        TextMuseSkinData skinData = TextMuseSkinData.load(this);
        if (skinData == null || skinData.skins == null || skinData.skins.size() <= 0) {
            finish();
            return;
        }

        mSkins = skinData.skins;

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mListView = (ListView) findViewById(R.id.skinSelectList);
        mAdapter = new SkinSelectListAdapter(this, mSkins);
        mListView.setAdapter(mAdapter);

        int selectedSkinId = TextMuseSkinData.getCurrentlySelectedSkin(this);
        mPreviousSkinId = selectedSkinId;
        mSelectedSkinIndex = 0;
        for (int i = 0; i < mSkins.size(); i++) {
            TextMuseSkin skin = mSkins.get(i);
            if (skin.skinId == selectedSkinId) {
                //i + 1 because position 0 is the regular skin
                mSelectedSkinIndex = i + 1;
            }
        }

        mListView.clearFocus();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(mSelectedSkinIndex);
                mListView.setItemChecked(mSelectedSkinIndex, true);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);

                //At this point, the selected skin index is the old one. Use it to keep track of the
                //previous skin ID in case of a failure
                if (mSelectedSkinIndex == 0) {
                    mPreviousSkinId = -1;
                } else {
                    TextMuseSkin skin = mSkins.get(mSelectedSkinIndex - 1);
                    mPreviousSkinId = skin.skinId;
                }

                if (position != mSelectedSkinIndex) {
                    if (position == 0) {
                        //-1 indicates the standard skin
                        selectSkinId(-1);
                    } else {
                        // -1 because position 0 is the standard skin
                        TextMuseSkin skin = mSkins.get(position - 1);
                        selectSkinId(skin.skinId);
                    }
                }

                mSelectedSkinIndex = position;
            }
        });
    }

    private void selectSkinId(int skinId) {
        if (mLaunchedFromSplash) {
            Toast toast = Toast.makeText(this, "Getting things ready...", Toast.LENGTH_SHORT);
            toast.show();
        }
        mProgressBar.setVisibility(View.VISIBLE);

//        if (!mLaunchedFromSplash) {
//            mProgressBar.setVisibility(View.VISIBLE);
//        }
        TextMuseSkinData.setCurrentlySelectedSkin(SkinSelectActivity.this, skinId);
        reloadMainData();
        registerPushNotifications();
    }

    protected void reloadMainData() {
        GlobalData globalData = GlobalData.getInstance();
        TextMuseData data = globalData.getData();

        mLoadingSkinChange = true;

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(this, getApplicationContext(), data == null ? -1 : data.appId);
        task.execute();
    }

    protected void registerPushNotifications() {
        AzureIntegrationSingleton azureIntegrationSingleton = AzureIntegrationSingleton.getInstance();
        azureIntegrationSingleton.startupIntegration(this.getApplicationContext());
    }

//    protected void reloadSkins() {
//        TextMuseSkinData skinData = TextMuseSkinData.load(this);
//        if (skinData == null || skinData.skins == null || skinData.skins.size() <= 0) {
//            finish();
//            return;
//        }
//
//        mSkins = skinData.skins;
//        mAdapter.setSkins(mSkins);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_skin_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_done) {
            if (mLaunchedFromSplash) {
                Intent intent = new Intent(this, WalkthroughActivity.class);
                intent.putExtra(WalkthroughActivity.INITIAL_LAUNCH_EXTRA, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            } else {
                setResult(Activity.RESULT_OK);

                mCloseInProcess = true;
                if (mLoadingSkinChange) {
                    //if we launched from settings, wait for the load to finish or else some weird things will happen
                    Toast toast = Toast.makeText(this, "Changing the skin, please wait...", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    finish();
                }
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);

        if (mLoadingSkinChange) {
            Toast toast = Toast.makeText(this, "Changing the skin, please wait...", Toast.LENGTH_LONG);
            toast.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void handleFetchResult(FetchNotesAsyncTask.FetchNotesResult result) {
        mProgressBar.setVisibility(View.GONE);
        mLoadingSkinChange = false;

        if (mCloseInProcess) {
            finish();
        }

        if (result == FetchNotesAsyncTask.FetchNotesResult.FETCH_FAILED) {
            if (!mLaunchedFromSplash) {
                Toast toast = Toast.makeText(this, "Could not change the skin of TextMuse. Please try again with a data connection.", Toast.LENGTH_LONG);
                toast.show();
            }
            TextMuseSkinData.setCurrentlySelectedSkin(SkinSelectActivity.this, mPreviousSkinId);
        } else {
            if (!mLaunchedFromSplash) {
                Toast toast = Toast.makeText(this, "Changed the skin successfully", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    public static class SkinSelectListAdapter extends ArrayAdapter<TextMuseSkin> {

        private Activity mActivity;
        private ArrayList<TextMuseSkin> mSkins;

        public static class ViewHolder {
            public TextView mTextView;
            public ImageView mImageView;
        }

        public SkinSelectListAdapter(Activity context, ArrayList<TextMuseSkin> skins) {
            super(context, R.layout.skin_select_list_ele, skins);
            mActivity = context;
            mSkins = skins;
        }

        public void setSkins(ArrayList<TextMuseSkin> skins) {
            mSkins = skins;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (rowView == null) {
                ViewHolder viewHolder = new ViewHolder();
                rowView = mActivity.getLayoutInflater().inflate(R.layout.skin_select_list_ele, parent, false);

                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.skinSelectListEleText);
                viewHolder.mImageView = (ImageView) rowView.findViewById(R.id.skinSelectListEleIcon);
                rowView.setTag(viewHolder);
            }

            ViewHolder viewHolder = (ViewHolder) rowView.getTag();

            if (position == 0) {
                //Position 0 is the standard textmuse item
                viewHolder.mTextView.setText("TextMuse");
                Picasso.with(mActivity)
                        .load(R.drawable.launcher_icon)
                        .into(viewHolder.mImageView);
            } else {
                TextMuseSkin skin = mSkins.get(position - 1);
                viewHolder.mTextView.setText(skin.name);
                Picasso.with(mActivity)
                        .load(skin.iconUrl)
                        .into(viewHolder.mImageView);
            }

            return rowView;
        }

        @Override
        public int getCount() {
            //Add one to the count since we always have the standard textmuse skin at position 0
            return mSkins.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }
    }
}
