package com.laloosh.textmuse.datamodel;

import java.util.ArrayList;
import java.util.Set;

public class TextMuseCurrentSkinData {
    public int skinId;
    public String name;
    public int c1;
    public int c2;
    public int c3;
    public String home;
    public String title;
    public String icon;
    public String masterName;
    public String masterIconUrl;

    public ArrayList<TextMuseLaunchIcon> launchIcons;


    public String getSplashImageFilename(TextMuseLaunchIcon icon, int count) {
        String result = "skin" + Integer.toString(this.skinId) + "_" + Integer.toString(icon.width) + "_" + Integer.toString(count) + ".jpg";
        return result;
    }

    public String getIconImageFilename() {
        return "icon_" + Integer.toString(this.skinId) + ".jpg";
    }

    public ArrayList<TextMuseLaunchIcon> getClosestSizeLaunchIcons(int width) {
        int closestSize = 0;
        int minWidthDifference = Integer.MAX_VALUE;
        for (TextMuseLaunchIcon icon : launchIcons) {
            int iconWidthDiff = Math.abs(width - icon.width);
            if (iconWidthDiff < minWidthDifference) {
                minWidthDifference = iconWidthDiff;
                closestSize = icon.width;
            }
        }

        ArrayList<TextMuseLaunchIcon> pickedLaunchIcons = new ArrayList<TextMuseLaunchIcon>();
        for (TextMuseLaunchIcon icon : launchIcons) {
            if (icon.width == closestSize) {
                pickedLaunchIcons.add(icon);
            }
        }

        return pickedLaunchIcons;
    }
}
