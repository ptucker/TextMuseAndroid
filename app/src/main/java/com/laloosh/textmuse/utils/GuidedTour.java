package com.laloosh.textmuse.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;

import java.util.ArrayList;
import java.util.HashMap;

public class GuidedTour {
    public enum GuidedTourSteps {INTRO, CONTENT, TEXTIT, CONTACT, DONE, SPONSOR, BADGE, REMIT};

    public interface Pause {
        void onComplete();
    }

    private HashMap<GuidedTourSteps, GuidedTourStep> steps;
    private Pause pause;

    public GuidedTour() {
        steps = new HashMap<>();
        steps.put(GuidedTourSteps.INTRO, new GuidedTourStep("Find fun events, great deals, and other stuff you can easily share with friends\n\nComplete this tour and you'll be entered into a drawing for a prize!"));
        steps.put(GuidedTourSteps.CONTENT, new GuidedTourStep("Now, see what secrets we already have for you.\n\nScroll down and tap on what you discover to share with a friend... a deal, a happy thought, or something fun to do!", R.drawable.choosecontent));
        steps.put(GuidedTourSteps.TEXTIT, new GuidedTourStep("Great. Tap on Text It so you can send this to your friends.", R.drawable.textit));
        steps.put(GuidedTourSteps.CONTACT, new GuidedTourStep("You're almost there! Pick one or more friends that you'd like to share this content with. Then send the message!", R.drawable.students));
        steps.put(GuidedTourSteps.DONE, new GuidedTourStep("Done! You're entered in our drawing for a gift certificate at the end of the month. Come back to TextMuse to see more great content, and to find out if you've won!"));

        steps.put(GuidedTourSteps.SPONSOR, new GuidedTourStep("Follow your favorite content sources! Click FOLLOW to get notified when %% adds new content. Content from %% will also appear in the Follows category.", R.drawable.follow));
        steps.put(GuidedTourSteps.BADGE, new GuidedTourStep("%% has a better deal for you. Share this with %% people and you'll get a badge that can be redeemed next time you visit %%."));
    }

    public GuidedTourStep getStepForKey(GuidedTourSteps step) {
        return steps.get(step);
    }

    public View addGuidedStepViewForKey(GuidedTourSteps step, Activity activity, ViewGroup container) {
        return addGuidedStepViewForKey(step, activity, container, null);
    }

    public View addGuidedStepViewForKey(GuidedTourSteps step, Activity activity, ViewGroup container, Pause p) {
        return addGuidedStepViewForKey(step, activity, container, p, null);
    }

    public View addGuidedStepViewForKey(GuidedTourSteps step, final Activity activity, ViewGroup container, Pause p, ArrayList<String> params) {
        final ViewGroup parent = container;
        final View view = activity.getLayoutInflater().inflate(R.layout.guidedtour_view, container, false);
        final GuidedTourSteps thisStep = step;
        pause = p;
        TextView tv = (TextView)view.findViewById(R.id.txtMessage);
        GuidedTourStep s = getStepForKey(step);
        String message = (params != null) ? insertParams(s.message, params) : s.message;
        tv.setText(message);
        ImageView img = (ImageView)view.findViewById(R.id.imgContent);
        img.setImageResource(s.imageResourceID);

        Button btnCancel = (Button)view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View b) {
                if (pause != null)
                    pause.onComplete();
                GlobalData.getInstance().getSettings().firstLaunch = false;
                GlobalData.getInstance().getSettings().save(activity.getApplicationContext());
                parent.removeView(view);
            }
        });

        Button btnOK = (Button)view.findViewById(R.id.btnContinue);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View b) {
                if (pause != null)
                    pause.onComplete();
                GlobalData.getInstance().getSettings().firstLaunch = (thisStep != GuidedTourSteps.DONE);
                GlobalData.getInstance().getSettings().save(activity.getApplicationContext());
                parent.removeView(view);
            }
        });

        container.addView(view);

        return view;
    }

    private String placeholder = "%%";
    private String insertParams(String msg, ArrayList<String>params) {
        String message = msg;
        if (params != null) {
            for (String p : params) {
                int i = message.indexOf(placeholder);
                if (i != -1) {
                    message = message.substring(0, i) + p + message.substring(i + placeholder.length());
                }
            }
        }

        return message;
    }

    public class GuidedTourStep {
        public String message;
        public int imageResourceID;

        public GuidedTourStep(String m, int i) {
            message = m; imageResourceID = i;
        }
        public GuidedTourStep(String m) {
            message = m; imageResourceID = R.drawable.banner2;
        }
    }

}
