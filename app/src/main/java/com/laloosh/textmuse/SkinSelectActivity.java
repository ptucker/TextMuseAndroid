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
import android.widget.TextView;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.TextMuseSkin;
import com.laloosh.textmuse.datamodel.TextMuseSkinData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SkinSelectActivity extends ActionBarActivity {

    public static final String EXTRA_LAUNCH_FROM_SPLASH = "launch_from_splash";

    ListView mListView;
    SkinSelectListAdapter mAdapter;
    ArrayList<TextMuseSkin> mSkins;
    int mSelectedSkinIndex;
    boolean mLaunchedFromSplash;

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

        mListView = (ListView) findViewById(R.id.skinSelectList);
        mAdapter = new SkinSelectListAdapter(this, mSkins);
        mListView.setAdapter(mAdapter);

        int selectedSkinId = TextMuseSkinData.getCurrentlySelectedSkin(this);
        mSelectedSkinIndex = 0;
        for (int i = 0; i < mSkins.size(); i++) {
            TextMuseSkin skin = mSkins.get(i);
            if (skin.skinId == selectedSkinId) {
                //i + 1 because position 0 is the regular skin
                mSelectedSkinIndex = i + 1;
            }
        }

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
        TextMuseSkinData.setCurrentlySelectedSkin(SkinSelectActivity.this, skinId);
        reloadMainData();
        registerPushNotifications();
    }

    protected void reloadMainData() {
        GlobalData globalData = GlobalData.getInstance();
        TextMuseData data = globalData.getData();

        FetchNotesAsyncTask task = new FetchNotesAsyncTask(null, getApplicationContext(), data == null ? -1 : data.appId);
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
            }

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onBackPressed() {
//        if (mLaunchedFromSplash) {
//            Intent intent = new Intent(this, WalkthroughActivity.class);
//            intent.putExtra(WalkthroughActivity.INITIAL_LAUNCH_EXTRA, true);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            finish();
//        } else {
//            super.onBackPressed();
//        }
//    }

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
