package com.example.identitydocumentsdk.barcodedetection;

import android.graphics.Bitmap;

public interface BarcodeProcessingInterface {
    void onDataLoaded(Bitmap idBack, String barcodeString);
    void onFailure(String message);
}
