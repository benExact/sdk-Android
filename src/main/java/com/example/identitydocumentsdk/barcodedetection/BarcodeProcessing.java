package com.example.identitydocumentsdk.barcodedetection;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.example.identitydocumentsdk.camera.CameraController;
import com.example.identitydocumentsdk.utils.APICalls;
import com.example.identitydocumentsdk.utils.DocTypeProvider;
import com.example.identitydocumentsdk.utils.ImageEdit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;

public class BarcodeProcessing extends AsyncTask<String, Integer, String> {

    private BarcodeProcessingInterface listener;
    private byte[] imageBytes;
    Camera.Parameters cameraParameters;
    FrameLayout fragmentLayout;
    Activity act;

    public BarcodeProcessing(FrameLayout fragmentLayout, Activity activity) {
        this.fragmentLayout = fragmentLayout;
        this.act = activity;
    }

    public void setImageBytes(byte[] imageBytes, Camera.Parameters parameters) {
        this.imageBytes = imageBytes;
        this.cameraParameters = parameters;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    private boolean detected = false;
    private String localDocType;

    public void setBarcodeProcessingListener(BarcodeProcessingInterface listener) {
        this.listener = listener;
    }

    private String processExtractedText(FirebaseVisionText firebaseVisionText) {

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        List<String> allLines = new ArrayList<>();
        String result = "";

        for (FirebaseVisionText.TextBlock line : blocks) {
            String text = line.getText();
            text = text.replace(" ", "");
            text = text.replace("\n", "");

            if (text.length() == 44 && text.contains("<")) {
                allLines.add(text);
            } else if (text.length() == 88 && text.contains("<")) {
                allLines.add(text);
            }
        }

        for (String value : allLines) {
            result = result + value;
        }

        Log.e("Text MRZ", "this is the MRZ text seen: " + result);
        return result;
    }

    @Override
    protected String doInBackground(String... strings) {
        int counter = 1;
        while (counter >= 0 && !isCancelled()) {
            localDocType = strings[0];
            final Bitmap image = ImageEdit.frameBytesToBitmap(imageBytes, cameraParameters);
            FirebaseVisionBarcodeDetectorOptions options;

            if (localDocType.equals(DocTypeProvider.IDCard)) {
                options =
                        new FirebaseVisionBarcodeDetectorOptions.Builder()
                                .setBarcodeFormats(
                                        FirebaseVisionBarcode.FORMAT_PDF417)
                                .build();
            } else {
                options =
                        new FirebaseVisionBarcodeDetectorOptions.Builder()
                                .setBarcodeFormats(
                                        FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                                .build();
            }
            FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(image);

            final FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                    .getVisionBarcodeDetector(options);

            detector.detectInImage(visionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                @Override
                public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {

                    if (firebaseVisionBarcodes.size() != 0) {
                        if (!detected) {

                            FirebaseVisionBarcode barcode = firebaseVisionBarcodes.get(0);
                            String rawValue = barcode.getRawValue();
                            detected = true;

                            if (localDocType.equals(DocTypeProvider.GreenBookID)) {
                                if (null != CameraController.camera){
                                    CameraController.camera.stopPreview();
                                    act.runOnUiThread(new Runnable(){
                                        @Override
                                        public void run() {
                                            fragmentLayout.setVisibility(View.GONE);
                                        } });
                                }
                                final String myUrl = "http://41.189.90.220/mmi/api/DocumentInformation/GetIDDocData";
                                final String IDValue = rawValue;
                                final String[] result = new String[1];

                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            APICalls api = new APICalls();
                                            result[0] = api.getIdDetails(myUrl,IDValue);
                                        }});
                                    t.start(); // spawn thread

                                try {
                                    t.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                    if (!result[0].equals("") || null != result[0]) {
                                        listener.onDataLoaded(image, result[0]);
                                    }
                            } else if (localDocType.equals(DocTypeProvider.Passport)) {
                                final FirebaseVisionImage fbImage = FirebaseVisionImage.fromBitmap(image);
                                final FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

                                detector.processImage(fbImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                        String result = processExtractedText(firebaseVisionText);
                                        int i = 0;
                                        if (listener != null && result.trim() != "") {
                                            listener.onDataLoaded(image, result);
                                        }
                                    }
                                });
                            } else {
                                listener.onDataLoaded(image, rawValue);
                            }
                        }
                    }
                }

                {
                    if (!detected) {
                        listener.onFailure("No barcode detected.");
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (!detected) {
                        listener.onFailure("An error occured while detecting barcode.");
                    }
                }
            });
        }
        return null;
    }
}
