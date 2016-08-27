package com.laloosh.textmuse.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseData;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BadgeFragment extends Fragment {

    @Bind(R.id.badgeExplorerCount) TextView mTextViewExplorerCount;
    @Bind(R.id.badgeMuseCount) TextView mTextViewMuseCount;
    @Bind(R.id.badgeSharerCount) TextView mTextViewSharerCount;
    @Bind(R.id.badgeExplorerDescription) View mExplorerDescription;
    @Bind(R.id.badgeMasterDescription) View mMasterDescription;
    @Bind(R.id.badgeMuseDescription) View mMuseDescription;
    @Bind(R.id.badgeSharerDescription) View mSharerDescription;
    @Bind(R.id.badgeMasterIcon) ImageView mMasterIcon;
    @Bind(R.id.badgeMasterTitle) TextView mMasterTitle;
    @Bind(R.id.badgeExplorerIcon) ImageView mExplorerIcon;
    @Bind(R.id.badgeMuseIcon) ImageView mMuseIcon;
    @Bind(R.id.badgeSharerIcon) ImageView mSharerIcon;

    private int mExplorerPoints;
    private int mMusePoints;
    private int mSharePoints;

    public BadgeFragment() {
        // Required empty public constructor
    }

    public static BadgeFragment newInstance() {
        BadgeFragment fragment = new BadgeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_badge, container, false);
        ButterKnife.bind(this, view);

        TextMuseData data = GlobalData.getInstance().getData();
        if (data != null) {
            if (data.skinData != null) {
                String master = data.skinData.masterName;
                String masterUrl = data.skinData.masterIconUrl;

                if (!TextUtils.isEmpty(master) && !TextUtils.isEmpty(masterUrl)) {
                    mMasterTitle.setText(master);
                    Glide.with(this)
                            .load(masterUrl)
                            .error(R.drawable.duckmaster)
                            .fitCenter()
                            .into(mMasterIcon);
                }
            }

            mExplorerPoints = data.explorerPoints;
            mMusePoints = data.musePoints;
            mSharePoints = data.sharerPoints;

            mTextViewExplorerCount.setText(String.format(Locale.US, "(%d/10)", mExplorerPoints));
            mTextViewMuseCount.setText(String.format(Locale.US, "(%d/10)", mMusePoints));
            mTextViewSharerCount.setText(String.format(Locale.US, "(%d/10)", mSharePoints));

            int totalBadges = 0;
            int totalPoints = 0;
            if (mExplorerPoints < 10) {
                mExplorerIcon.setColorFilter(0x55000000);
            } else {
                totalBadges++;
            }
            totalPoints += mExplorerPoints;

            if (mMusePoints < 10) {
                mMuseIcon.setColorFilter(0x55000000);
            } else {
                totalBadges++;
            }
            totalPoints += mMusePoints;

            if (mSharePoints < 10) {
                mSharerIcon.setColorFilter(0x55000000);
            } else {
                totalBadges++;
            }
            totalPoints += mSharePoints;

            if (totalBadges < 2 && totalPoints < 25) {
                mMasterIcon.setColorFilter(0x55000000);
            }
        }
        resetDescriptions();

        return view;
    }

    protected void resetDescriptions() {
        mExplorerDescription.setVisibility(View.GONE);
        mMasterDescription.setVisibility(View.GONE);
        mMuseDescription.setVisibility(View.GONE);
        mSharerDescription.setVisibility(View.GONE);
    }

    @OnClick (R.id.badgeExplorerLayout)
    public void clickExplorer() {
        int visibility = mExplorerDescription.getVisibility();
        resetDescriptions();
        if (visibility != View.VISIBLE) {
            mExplorerDescription.setVisibility(View.VISIBLE);
        }
    }

    @OnClick (R.id.badgeMasterLayout)
    public void clickMaster() {
        int visibility = mMasterDescription.getVisibility();
        resetDescriptions();
        if (visibility != View.VISIBLE) {
            mMasterDescription.setVisibility(View.VISIBLE);
        }
    }

    @OnClick (R.id.badgeMuseLayout)
    public void clickMuse() {
        int visibility = mMuseDescription.getVisibility();
        resetDescriptions();
        if (visibility != View.VISIBLE) {
            mMuseDescription.setVisibility(View.VISIBLE);
        }
    }

    @OnClick (R.id.badgeSharerLayout)
    public void clickSharer() {
        int visibility = mSharerDescription.getVisibility();
        resetDescriptions();
        if (visibility != View.VISIBLE) {
            mSharerDescription.setVisibility(View.VISIBLE);
        }
    }
}
