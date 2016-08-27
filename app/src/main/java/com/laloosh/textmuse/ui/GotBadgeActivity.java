package com.laloosh.textmuse.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.TextMuseCurrentSkinData;
import com.laloosh.textmuse.datamodel.TextMuseData;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GotBadgeActivity extends Activity {
    public static final String BADGE_EXTRA = "badge_extra";

    public static final String BADGE_EXPLORER = "explorer";
    public static final String BADGE_MUSE = "muse";
    public static final String BADGE_SHARER = "sharer";
    public static final String BADGE_MASTER = "master";

    @Bind(R.id.gotBadgeBackground) View mBackground;
    @Bind(R.id.gotBadgeIcon) ImageView mIcon;
    @Bind(R.id.gotBadgeText) TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_got_badge);
        ButterKnife.bind(this);

        String badge = getIntent().getStringExtra(BADGE_EXTRA);
        if (badge == null) {
            finish();
            return;
        }

        mBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextMuseData data = GlobalData.getInstance().getData();
        TextMuseCurrentSkinData skinData = data.skinData;

        if (badge.equals(BADGE_EXPLORER)) {
            mBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.explorer_background));
            mIcon.setImageResource(R.drawable.explorer);
            mText.setText(getString(R.string.explorer_congrats));
        } else if (badge.equals(BADGE_MUSE)) {
            mBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.muse_background));
            mIcon.setImageResource(R.drawable.muse);
            mText.setText(getString(R.string.muse_congrats));
        } else if (badge.equals(BADGE_SHARER)) {
            mBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.sharer_background));
            mIcon.setImageResource(R.drawable.sharer);
            mText.setText(getString(R.string.sharer_congrats));
        } else if (badge.equals(BADGE_MASTER)) {
            mBackground.setBackgroundColor(ContextCompat.getColor(this, R.color.master_background));
            if (skinData != null && !TextUtils.isEmpty(skinData.masterIconUrl) && !TextUtils.isEmpty(skinData.masterName)) {
                Glide.with(this)
                        .load(skinData.masterIconUrl)
                        .error(R.drawable.duckmaster)
                        .fitCenter()
                        .into(mIcon);
                mText.setText(skinData.masterName);
            } else {
                mIcon.setImageResource(R.drawable.muse);
                mText.setText(getString(R.string.muse_congrats));
            }
        }
    }
}
