package com.example.identitydocumentsdk.ui;

import android.view.animation.Interpolator;

public class ScanningInterpolator implements Interpolator {
//    boolean isBottom = false;
//    float backCounter = 1;
    float radianRatio = 1f / (float)(2 * Math.PI);
    @Override
    public float getInterpolation(float input) {
//        if (input >= 0.5){
//            isBottom = true;
//        }else{
//            isBottom = false;
//        }
//
//        if (isBottom){
//            return 2f- (input*2);
//        }else{
//            backCounter = 1;
//            return input + input;
//        }
            return (float) ((0.5 * Math.sin((input / radianRatio))) + 0.5);
    }
}
