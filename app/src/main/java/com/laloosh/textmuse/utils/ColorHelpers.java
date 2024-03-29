package com.laloosh.textmuse.utils;

import com.laloosh.textmuse.app.Constants;

public class ColorHelpers {

    public static int getTextColorForBackground(int backgroundColor) {

        //Hard-code in the normal skin colors, since they measure out to be on the border
        //of the brightness scale
        for (int color : Constants.COLOR_LIST) {
            if (backgroundColor == color) {
                return 0xFFFFFFFF;
            }
        }

        int brightness = getColorBrightness(backgroundColor);

        //if the background is darker, return white, otherwise return black
        return (brightness < 128) ? 0xFFFFFFFF : 0xFF000000;
    }

    public static int getTextColorForWhiteBackground(int color) {
        //Hard-code check for normal skin colors, because they are borderline on the
        //brightness scale
        for (int c : Constants.COLOR_LIST) {
            if (c == color) {
                return color;
            }
        }

        int brightness = getColorBrightness(color);

        //if the text is too bright, then return a dark gray, otherwise just return the color
        return (brightness > 128) ? 0xFF595959 : color;
    }

    public static int getColorBrightness(int color) {

        //Convert RGBs to doubles for calculations
        double r = (double) ((color & 0x00FF0000) >> 16);
        double g = (double) ((color & 0x0000FF00) >> 8);
        double b = (double) (color & 0x000000FF);

        //This formula calculates the brightness of a color ranging from 0 (black) to 255 (white)
        //From here: http://alienryderflex.com/hsp.html
        int brightness = (int) Math.sqrt((0.299 * r * r) + (0.587 * g * g) + (0.114 * b * b));

        return brightness;
    }
}
