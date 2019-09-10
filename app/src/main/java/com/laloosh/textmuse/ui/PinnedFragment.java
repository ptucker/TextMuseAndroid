package com.laloosh.textmuse.ui;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.Note;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.datamodel.events.TabDeselectedEvent;
import com.laloosh.textmuse.datamodel.events.TabSelectedEvent;
import com.laloosh.textmuse.utils.ColorHelpers;

import java.util.List;

import de.greenrobot.event.EventBus;


public class PinnedFragment extends Fragment {
    public static final int TAB_NUMBER = 1;

    private TextMuseData mData;
    private ListView mListView;
    private PinnedNoteAdapter mAdapter;
    private TextView mTextView;

//    private ArrayList<NoteExtended> mSortedNotes;


    public PinnedFragment() {
        // Required empty public constructor
    }

    public static PinnedFragment newInstance() {
        PinnedFragment fragment = new PinnedFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pinned, container, false);

        mAdapter = null;

        GlobalData instance = GlobalData.getInstance();
        if (!instance.hasLoadedData()) {
            instance.loadData(this.getContext());
        }
        mData = instance.getData();

        mTextView = (TextView) v.findViewById(R.id.pinnedFragmentTextView);

        mListView = (ListView) v.findViewById(R.id.pinnedFragmentListView);
        mListView.setVisibility(View.GONE);

        generateViewsFromData();

        return v;
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

    public void onEvent(TabSelectedEvent event) {
        if (event.tabNumber == TAB_NUMBER) {
            generateViewsFromData();
        }
    }

    public void onEvent(TabDeselectedEvent event) {
        if (event.tabNumber == TAB_NUMBER) {
            //Don't actually need to do anything when we deselect a tab
        }
    }

    private void generateViewsFromData() {
        if (mData != null && mData.pinnedNotes != null && mData.pinnedNotes.notes != null && mData.pinnedNotes.notes.size() > 0) {

            if (mAdapter != null) {
                mAdapter.updateNotes();
            } else {

                mAdapter = new PinnedNoteAdapter(this.getContext(), mData.pinnedNotes.notes, mData);
                mListView.setAdapter(mAdapter);
            }

            mListView.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.INVISIBLE);
        }
    }

    public static class PinnedNoteAdapter extends ArrayAdapter<Note> {

        private Context mContext;
        private List<Note> mNotes;
        private TextMuseData mData;

        //view holder pattern to prevent repeated queries for ID
        static class ViewHolder {
            public TextView mCategoryTitle;
            public ImageView mArrow;

            public View mBackgroundViewTextOnly;
            public TextView mTextView;
            public ImageView mBackgroundImageView;

            public ImageView mSendImageView;

            public ViewGroup mLayoutSend;

            public ViewGroup mTextLayout;

            public boolean mTextOnly;
        }

        public PinnedNoteAdapter(Context context, List<Note> notes, TextMuseData data) {
            super(context, R.layout.list_ele_category, notes);

            this.mContext = context;
            this.mData = data;
            this.mNotes = notes;
        }


        @Override
        public int getCount() {
            return mNotes.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            final Note note = mNotes.get(position);

            if (rowView == null) {

                LayoutInflater inflater = LayoutInflater.from(mContext);
                ViewHolder viewHolder = new ViewHolder();

                if (note.hasDisplayableMedia()) {
                    rowView = inflater.inflate(R.layout.list_ele_category_textimage3, parent, false);
                    viewHolder.mBackgroundImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewItemBackground);
                    viewHolder.mTextOnly = false;
                } else {
                    rowView = inflater.inflate(R.layout.list_ele_category_textonly3, parent, false);
                    viewHolder.mBackgroundViewTextOnly = rowView.findViewById(R.id.mainViewBackgroundView);
                    viewHolder.mTextOnly = true;
                }

                viewHolder.mTextView = (TextView) rowView.findViewById(R.id.mainViewTextViewText);
                viewHolder.mCategoryTitle = (TextView) rowView.findViewById(R.id.mainFragmentListItemTextViewTitle);
                viewHolder.mArrow = (ImageView) rowView.findViewById(R.id.mainFragmentListItemImageArrow);
                viewHolder.mTextLayout = (ViewGroup) rowView.findViewById(R.id.mainViewRelativeLayoutTextItem);
                viewHolder.mSendImageView = (ImageView) rowView.findViewById(R.id.mainViewImageViewSend);

                viewHolder.mLayoutSend = (ViewGroup) rowView.findViewById(R.id.mainViewLayoutSend);

                rowView.setTag(viewHolder);
            }

            final ViewHolder holder = (ViewHolder) rowView.getTag();

            TextMuseData data = GlobalData.getInstance().getData();
            int[] colorList = data.getColorList();
            int color = colorList[position % colorList.length];

            holder.mCategoryTitle.setText("Pinned Notes");
            holder.mCategoryTitle.setTextColor(ColorHelpers.getTextColorForWhiteBackground(color));

            holder.mArrow.setColorFilter(color);

            View.OnClickListener onCategoryClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SelectMessageActivity.class);
                    intent.putExtra(SelectMessageActivity.CATEGORY_EXTRA, mData.categories.size() + 2);
                    intent.putExtra(SelectMessageActivity.COLOR_OFFSET_EXTRA, position);
                    mContext.startActivity(intent);
                }
            };

            View.OnClickListener onNoteClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SelectMessageActivity.class);
                    intent.putExtra(SelectMessageActivity.CATEGORY_EXTRA, mData.categories.size() + 2);
                    intent.putExtra(SelectMessageActivity.COLOR_OFFSET_EXTRA, position);
                    intent.putExtra(SelectMessageActivity.NOTE_INDEX_EXTRA, position);
                    mContext.startActivity(intent);
                }
            };

            holder.mArrow.setOnClickListener(onCategoryClickListener);
            holder.mCategoryTitle.setOnClickListener(onCategoryClickListener);
            holder.mTextLayout.setOnClickListener(onNoteClickListener);

            //Default the text color to white unless we change it
            holder.mTextView.setTextColor(0xFFFFFFFF);

            if (!note.hasDisplayableText()) {
                holder.mTextView.setVisibility(View.INVISIBLE);
            } else {
                holder.mTextView.setVisibility(View.VISIBLE);
                holder.mTextView.setText(note.getText());
            }

            if (!note.hasDisplayableMedia()) {
                holder.mBackgroundViewTextOnly.setBackgroundColor(color);
                holder.mTextView.setTextColor(ColorHelpers.getTextColorForBackground(color));
            } else {

                holder.mBackgroundImageView.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(mContext)
                                .load(note.getDisplayMediaUrl(mContext))
                                .error(R.drawable.placeholder_image)
                                .fitCenter()
                                .override(holder.mBackgroundImageView.getWidth(), Target.SIZE_ORIGINAL)
                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                .into(holder.mBackgroundImageView);
                    }
                });
            }

            holder.mSendImageView.setColorFilter(0xff1a1a1a);

            holder.mLayoutSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ContactsPickerActivity.class);
                    intent.putExtra(ContactsPickerActivity.CATEGORY_POSITION_EXTRA, mData.categories.size() + 2);
                    intent.putExtra(ContactsPickerActivity.NOTE_POSITION_EXTRA, position);
                    intent.putExtra(ContactsPickerActivity.NOTE_ID_EXTRA, note.noteId);
                    mContext.startActivity(intent);
                }
            });

            return rowView;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            //Type 0 is an image one, type 1 is the text one
            final Note note = mNotes.get(position);
            if (note.hasDisplayableMedia()) {
                return 0;
            } else {
                return 1;
            }
        }

        public void updateNotes() {
            mData = GlobalData.getInstance().getData();
            mNotes = mData.pinnedNotes.notes;
            this.notifyDataSetChanged();
        }
    }
}
