package com.laloosh.textmuse;

import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.internal.widget.DrawableUtils;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;

import java.util.ArrayList;

public class SortableCategoryAdapter
        extends RecyclerView.Adapter<SortableCategoryAdapter.CategoryViewHolder>
        implements DraggableItemAdapter<SortableCategoryAdapter.CategoryViewHolder> {

    public static class CategoryViewHolder extends AbstractDraggableItemViewHolder {
        public FrameLayout mContainer;
        public TextView mTextView;
        public ImageView mLockImageView;

        public CategoryViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.settingsTextViewCategoryName);
            mContainer = (FrameLayout) v.findViewById(R.id.settingsCategoryContainer);
            mLockImageView = (ImageView) v.findViewById(R.id.settingsCategoryLockImage);
        }
    }

    private ArrayList<String> mCategoryList;
    private int mFirstUnlockedIndex;

    public SortableCategoryAdapter(ArrayList<String> categoryList) {

        // DraggableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
        mCategoryList = categoryList;

        mFirstUnlockedIndex = 0;
        for (int i = 0; i < mCategoryList.size(); i++) {
            String c = mCategoryList.get(i);
            if (c.equalsIgnoreCase("Trending") || c.equalsIgnoreCase("Highlighted")) {
                continue;
            } else {
                Log.d(Constants.TAG, "First unlocked index: " + i);
                mFirstUnlockedIndex = i;
                break;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return mCategoryList.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        //Only one view type
        return 0;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.settings_category_ele_draggable, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {
        String itemText = mCategoryList.get(position);

        // set text
        holder.mTextView.setText(itemText);

        // Lock image for the trending and highlighted
        if (itemText.equalsIgnoreCase("Trending") || itemText.equalsIgnoreCase("Highlighted")) {
            holder.mLockImageView.setVisibility(View.VISIBLE);
        } else {
            holder.mLockImageView.setVisibility(View.GONE);
        }

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();

        if (((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_UPDATED) != 0)) {
            int bgResId;

            if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.
                Drawable drawable = holder.mContainer.getForeground();
                drawable.setState(new int[]{});
            } else if ((dragState & RecyclerViewDragDropManager.STATE_FLAG_DRAGGING) != 0) {
                bgResId = R.drawable.bg_item_dragging_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        Log.d(Constants.TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");

        if (fromPosition == toPosition) {
            return;
        }

        String item = mCategoryList.remove(fromPosition);
        mCategoryList.add(toPosition, item);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(CategoryViewHolder holder, int position, int x, int y) {
        String categoryName = mCategoryList.get(position);

        if (categoryName.equalsIgnoreCase("Trending") || categoryName.equalsIgnoreCase("Highlighted")) {
            return false;
        }

        return true;

//        // x, y --- relative from the itemView's top-left
//        final View containerView = holder.mContainer;
//        final View dragHandleView = holder.mDragHandle;
//
//        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
//        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);
//
//        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY);
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(CategoryViewHolder holder, int position) {
        return new ItemDraggableRange(mFirstUnlockedIndex, mCategoryList.size() - 1);
    }
}
