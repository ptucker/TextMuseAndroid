package com.laloosh.textmuse.utils;

import android.content.Context;
import android.content.Intent;

import com.laloosh.textmuse.datamodel.GlobalData;
import com.laloosh.textmuse.datamodel.PointUpdate;
import com.laloosh.textmuse.datamodel.TextMuseData;
import com.laloosh.textmuse.ui.GotBadgeActivity;

public class PointsHelper {

    //Given a string input (result of a REST call), checks and saves our point totals
    public static void checkPoints(String s, Context context) {
        WebDataParser parser = new WebDataParser();
        PointUpdate update = parser.parsePointUpdate(s);
        if (update != null) {
            TextMuseData data = GlobalData.getInstance().getData();

            int oldSharerPoints = data.sharerPoints;
            int oldMusePoints = data.musePoints;
            int oldExplorerPoints = data.explorerPoints;

            data.sharerPoints = update.sp;
            data.musePoints = update.mp;
            data.explorerPoints = update.ep;
            data.save(context);

            if (!data.gotMasterBadge) {
                int total = update.sp + update.mp + update.ep;
                if (total >= 25) {
                    data.gotMasterBadge = true;
                }
                int totalBadges = 0;
                if (update.sp >= 10) {
                    totalBadges++;
                }
                if (update.mp >= 10) {
                    totalBadges++;
                }
                if (update.ep >= 10) {
                    totalBadges++;
                }
                if (totalBadges > 1) {
                    data.gotMasterBadge = true;
                }

                if (data.gotMasterBadge) {
                    Intent intent = new Intent(context, GotBadgeActivity.class);
                    intent.putExtra(GotBadgeActivity.BADGE_EXTRA, GotBadgeActivity.BADGE_MASTER);
                    context.startActivity(intent);
                    data.save(context);
                    return;
                }
            }

            if (oldSharerPoints < 10 && update.sp >= 10) {
                Intent intent = new Intent(context, GotBadgeActivity.class);
                intent.putExtra(GotBadgeActivity.BADGE_EXTRA, GotBadgeActivity.BADGE_SHARER);
                context.startActivity(intent);
            } else if (oldMusePoints < 10 && update.mp >= 10) {
                Intent intent = new Intent(context, GotBadgeActivity.class);
                intent.putExtra(GotBadgeActivity.BADGE_EXTRA, GotBadgeActivity.BADGE_MUSE);
                context.startActivity(intent);
            } else if (oldExplorerPoints < 10 && update.ep >= 10) {
                Intent intent = new Intent(context, GotBadgeActivity.class);
                intent.putExtra(GotBadgeActivity.BADGE_EXTRA, GotBadgeActivity.BADGE_EXPLORER);
                context.startActivity(intent);
            }
        }
    }
}
