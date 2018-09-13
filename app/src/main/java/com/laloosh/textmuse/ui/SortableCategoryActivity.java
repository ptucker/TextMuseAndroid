package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.SimpleListDividerDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseSettings;

public class SortableCategoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    private TextMuseSettings mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sortable_category);


        GlobalData globalData = GlobalData.getInstance();
        if (!globalData.hasLoadedData()) {
            globalData.loadData(this);
        }

        mSettings = globalData.getSettings();
        if (mSettings == null) {
            finish();
            return;
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setInitiateOnLongPress(true);

        SortableCategoryAdapter adapter = new SortableCategoryAdapter(mSettings.getCategoryOrder());
        mAdapter = adapter;
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(adapter);

        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);
        mRecyclerView.setItemAnimator(animator);
        mRecyclerView.addItemDecoration(new SimpleListDividerDecorator(getResources().getDrawable(R.drawable.list_divider), true));

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (mRecyclerView != null) {
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }

        if (mWrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sortable_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home || id == R.id.action_done) {

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
        //the category order might have changed, so let's save it
        mSettings.save(this);
        setResult(Activity.RESULT_OK);
    }
}
