package com.example.identitydocumentsdk;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import com.example.identitydocumentsdk.camera.IDScanActivity;
import com.example.identitydocumentsdk.models.IDModel;

public class SybrinSmartIdentification extends ContextWrapper {
    private static SybrinSmartIdentification single_instance = null;
    private IDModel model;
    private SybrinSmartIdentificationInterface listener;

    private SybrinSmartIdentification(Context base) {
        super(base);
    }

//    public SybrinSmartIdentification() {
//    }

    public IDModel getModel() {
        return model;
    }

    public static SybrinSmartIdentification getInstance(Context base)
    {
        if (single_instance == null)
            single_instance = new SybrinSmartIdentification(base);

        return single_instance;
    }

    public void setListener(SybrinSmartIdentificationInterface listener) {
        this.listener = listener;
    }

    public void setModel(IDModel model){
        this.model = model;
        if (null != listener){
            listener.onDataReturned(this.model);
        }
    }

    public void StartSmartIdentification(String docType){
        Intent i = new Intent(getApplicationContext(), IDScanActivity.class);
        i.putExtra("docType", docType);
        startActivity(i);
    }
}
