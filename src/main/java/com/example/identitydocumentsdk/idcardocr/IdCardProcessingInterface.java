package com.example.identitydocumentsdk.idcardocr;

import android.graphics.Bitmap;

public interface IdCardProcessingInterface {
    void onDataLoaded(Bitmap idFront, String ocrString);
    void onFailure(String message);
}
