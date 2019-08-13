package com.example.identitydocumentsdk.camera;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.identitydocumentsdk.R;
import com.example.identitydocumentsdk.SybrinSmartIdentification;
import com.example.identitydocumentsdk.facialdetection.FacialProcessing;
import com.example.identitydocumentsdk.facialdetection.FacialProcessingInterface;
import com.example.identitydocumentsdk.models.IDModel;
import com.example.identitydocumentsdk.ui.SnackHandler;
import com.example.identitydocumentsdk.utils.ConnectionHandler;
import com.example.identitydocumentsdk.utils.DocTypeProvider;
import com.example.identitydocumentsdk.utils.IDBookModelMapping;
import com.example.identitydocumentsdk.utils.IDModelMapping;
import com.example.identitydocumentsdk.utils.PassportModelMapping;
import com.example.identitydocumentsdk.utils.PermissionsHandler;
import java.util.ArrayList;

public class IDScanActivity extends AppCompatActivity {
    private String barcodeString;
    private Bitmap idFrontImage;
    private Bitmap idBackImage;
    private Bitmap idFaceImage;
    private String docType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getSupportActionBar().hide();
        setContentView(R.layout.id_scan_activity);

        docType = getDocType();
        try {
            if (null != docType) {
                if (docType.equals(DocTypeProvider.GreenBookID) && !ConnectionHandler.checkInternetConnection(this)) {
                    SnackHandler.showMessage(this, "Internet connection required.", Toast.LENGTH_LONG);
                    finish();
                } else {
                    boolean permissionsResult = PermissionsHandler.handlePermissions(IDScanActivity.this);
                    if (permissionsResult) {
                        launchCameraFragment();
                    }
                }
            } else {
                throw new Exception("No document type provided");
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private String getDocType() {
        Intent i = getIntent();
        return i.getStringExtra("docType");
    }

    private void launchCameraFragment() {
                CameraPreviewFragment frag = new CameraPreviewFragment();

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.IdScanlayout, frag)
                        .addToBackStack("preview")
                        .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {
            ArrayList<Integer> unGrantedPermissions = new ArrayList<Integer>();

            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    unGrantedPermissions.add(grant);
                }
            }

            if (unGrantedPermissions.isEmpty()) {
                launchCameraFragment();
            } else {
                throw new Exception("Required permissions not granted");
            }
        } catch (Exception e) {
            finish();
            e.printStackTrace();
        }
    }

    private void showLoader(){
        ProgressBar pBar = findViewById(R.id.pBar);
        pBar.setVisibility(View.VISIBLE);
    }

    public void processCapturedData(Bitmap idFront, Bitmap idBackp, String barcodeString) {


        this.barcodeString = barcodeString;
        final String docType = getDocType();
        idBackImage = idBackp;
        final long[] startTime = new long[1];
        startTime[0] = System.nanoTime();
        final long[] endTime = new long[1];
        FacialProcessing facial = new FacialProcessing();
        facial.cropFace(idFront, docType);
        showLoader();
        facial.setFacialProcessingListener(new FacialProcessingInterface() {
            @Override
            public void onDataLoaded(Bitmap image, Bitmap croppedFace) {
//                    try {
                idFrontImage = image;
                idFaceImage = croppedFace;

                if (docType.equals(DocTypeProvider.IDCard)) {

                } else {
                    idBackImage = image;
                }
                endTime[0] = System.nanoTime();
                long duration = (endTime[0] - startTime[0]) / 1000000;
                duration = duration / 1000;
                finishActivity(buildIdModel(docType));
            }

            @Override
            public void onFailure(String message) {
                //Do Nothing for now
            }
        });

    }

    private IDModel buildIdModel(String docType) {

        IDModel dataModel = new IDModel();

        if (docType.equals(DocTypeProvider.IDCard)) {
            IDModelMapping mapData = new IDModelMapping();
            dataModel = mapData.MapCardData(barcodeString, docType);
            dataModel.BackImage = idBackImage;
            dataModel.DocumentImage = idFrontImage;
            dataModel.PortraitImage = idFaceImage;

            return dataModel;
        } else if (docType.equals(DocTypeProvider.GreenBookID)) {
            IDBookModelMapping idBookModelMapping = new IDBookModelMapping();
            dataModel = idBookModelMapping.MapCardData(barcodeString, docType);
            dataModel.BackImage = idFrontImage;
            dataModel.DocumentImage = idFrontImage;
            dataModel.PortraitImage = idFaceImage;

//            String serilizedData = new Gson().toJson(dataModel);

            return dataModel;
        } else if (docType.equals(DocTypeProvider.Passport)) {
            PassportModelMapping passportModelMapping = new PassportModelMapping();
            dataModel = passportModelMapping.MapCardData(barcodeString, docType);
            dataModel.BackImage = idFrontImage;
            dataModel.DocumentImage = idFrontImage;
            dataModel.PortraitImage = idFaceImage;

            return dataModel;
        } else {
            return dataModel;
        }


    }

    private void finishActivity(IDModel dataModel) {
        SybrinSmartIdentification id = SybrinSmartIdentification.getInstance(IDScanActivity.this);
        id.setModel(dataModel);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
