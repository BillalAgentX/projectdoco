package com.projectdocupro.mobile.utility;

import android.view.View;

import java.util.List;

public class RotationHelper {

    public RotationHelper() {
    }

    /**
     * rotate all imagebuttons stored in the given list from a specific degree value
     * to a specific dregree value.
     *
     * @param fromDegrees - the start degree value
     * @param toDegree    - the target degree value
     * @param list        - the list that stored the imagebuttons to be rotated
     */
    public void rotate(float fromDegrees, float toDegree, List<View> list) {
//        final RotateAnimation rotateAnim = new RotateAnimation(fromDegrees, toDegree, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
//
//        rotateAnim.setDuration(250);
//        rotateAnim.setFillAfter(true);

        for (View imageButton : list) {
            if (imageButton.getVisibility() != View.GONE) {
//                imageButton.startAnimation(rotateAnim);
                imageButton.setRotation(toDegree);
            }
        }
    }
}