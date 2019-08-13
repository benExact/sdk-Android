package com.example.identitydocumentsdk.facialdetection;


import android.graphics.Bitmap;

public interface  FacialProcessingInterface {
     void onDataLoaded(Bitmap image, Bitmap croppedFace);
     void onFailure(String message);
}
