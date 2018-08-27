package com.laloosh.textmuse.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.GlobalData;

import java.util.HashMap;

public class GuidedTour {
    public enum GuidedTourSteps {INTRO, CONTENT, TEXTIT, CONTACT, DONE};

    public interface Pause {
        void onComplete();
    }

    private HashMap<GuidedTourSteps, GuidedTourStep> steps;
    private Pause pause;

    public GuidedTour() {
        steps = new HashMap<>();
        steps.put(GuidedTourSteps.INTRO, new GuidedTourStep("Welcome to the Guided Tour for TextMuse. You'll find a lot of great content here that you'll want to share with friends. When you complete this tour you'll be entered into a drawing.\n\nFirst, choose your version, so you can get the most relevant content. You also have to option to register."));
        steps.put(GuidedTourSteps.CONTENT, new GuidedTourStep("Now, check out the great content we already have for you. Scroll through and find something you're interested in and tap on it to see details.", R.drawable.choosecontent));
        steps.put(GuidedTourSteps.TEXTIT, new GuidedTourStep("Great. Tap on Text It so you can send this to your friends.", R.drawable.textit));
        steps.put(GuidedTourSteps.CONTACT, new GuidedTourStep("You're almost there! Pick one or more friends that you'd like to share this content with. Then send the message!", R.drawable.students));
        steps.put(GuidedTourSteps.DONE, new GuidedTourStep("Done! You're entered in our drawing for a gift certificate at the end of the month. Come back to TextMuse to see more great content, and to find out if you've won!"));
    }

    public GuidedTourStep getStepForKey(GuidedTourSteps step) {
        return steps.get(step);
    }

    public View addGuidedStepViewForKey(GuidedTourSteps step, Activity activity, ViewGroup container) {
        return addGuidedStepViewForKey(step, activity, container, null);
    }

    public View addGuidedStepViewForKey(GuidedTourSteps step, Activity activity, ViewGroup container, Pause p) {
        final ViewGroup parent = container;
        final View view = activity.getLayoutInflater().inflate(R.layout.guidedtour_view, container, false);
        final GuidedTourSteps thisStep = step;
        pause = p;
        TextView tv = (TextView)view.findViewById(R.id.txtMessage);
        GuidedTourStep s = steps.get(step);
        tv.setText(s.message);
        ImageView img = (ImageView)view.findViewById(R.id.imgContent);
        img.setImageResource(s.imageResourceID);

        Button btnCancel = (Button)view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View b) {
                if (pause != null)
                    pause.onComplete();
                GlobalData.getInstance().getSettings().firstLaunch = false;
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
                parent.removeView(view);
            }
        });

        container.addView(view);

        return view;
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
