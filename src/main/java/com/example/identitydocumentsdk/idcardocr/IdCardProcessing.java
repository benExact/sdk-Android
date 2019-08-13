package com.example.identitydocumentsdk.idcardocr;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.identitydocumentsdk.barcodedetection.BarcodeProcessing;
import com.example.identitydocumentsdk.utils.DocTypeProvider;
import com.example.identitydocumentsdk.utils.ImageEdit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdCardProcessing extends AsyncTask<String, Integer, String> {

    private IdCardProcessingInterface listener;
    private boolean isDetecting = false;
    private int frameCount = 0;
    private byte[] imageBytes;
    Camera.Parameters cameraParameters;
    private String DocType = "";

    public void setImage(byte[] imageBytes, Camera.Parameters parameters) {
        this.imageBytes = imageBytes;
        this.cameraParameters = parameters;
    }

    public void setDocType(String docType) {
        this.DocType = docType;
    }

    private static final Pattern sPattern
            = Pattern.compile("(((\\d{2}((0[13578]|1[02])(0[1-9]|[12]\\d|3[01])|(0[13456789]|1[012])(0[1-9]|[12]\\d|30)|02(0[1-9]|1\\d|2[0-8])))|([02468][048]|[13579][26])0229))(( |-)(\\d{4})( |-)(\\d{3})|(\\d{7}))");
    boolean isID = false;


    public void setIdentityCardProcessingListener(IdCardProcessingInterface listener) {
        this.listener = listener;
    }

    private String processExtractedText(FirebaseVisionText firebaseVisionText) {
        String result = "";
        String[] lines = firebaseVisionText.getText().split("\\r?\\n");

        for (String line : lines) {
            line = line.replaceAll("\\D+", "");
            Matcher m = sPattern.matcher(line);
            if (m.find() && line.length() == 13) {
                isID = true;

                return line;
            }
        }
        return result;
    }

    private String processExtractedTextMRZ(FirebaseVisionText firebaseVisionText) {

        List<FirebaseVisionText.TextBlock> blocks = firebaseVisionText.getTextBlocks();
        List<String> allLines = new ArrayList<>();
        String result = "";

        for (FirebaseVisionText.TextBlock line : blocks) {
            String text = line.getText();
            text = text.replace(" ", "");
            text = text.replace("\n", "");

            if (text.length() == 44 && text.contains("<")) {
                text.replaceAll("0", "O");
                text.replaceAll("6", "G");
                text.replaceAll("1", "I");

                allLines.add(text);
            } else if (text.length() == 88 && text.contains("<")) {
                String mrzLine1 = text.substring(0, 44);
                String mrzLine2 = text.substring(44);

                mrzLine1.replaceAll("0", "O");
                mrzLine1.replaceAll("6", "G");
                mrzLine1.replaceAll("1", "I");


                allLines.add(mrzLine1 + mrzLine2);
            }
        }

        for (String value : allLines) {
            result = result + value;
        }

        Log.e("Text MRZ", "this is the MRZ text seen: " + result);
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }

    @Override
    protected String doInBackground(String... strings) {
        int counter = 1;
        final boolean[] busy = new boolean[1];
        busy[0] = false;
        while (counter >= 0 && !isCancelled()) {
            final Bitmap image = ImageEdit.frameBytesToBitmap(imageBytes, cameraParameters);
            final FirebaseVisionImage fbImage = FirebaseVisionImage.fromBitmap(image);
            final FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
            if (!busy[0]) {
                busy[0] = true;
                detector.processImage(fbImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        String result = "";
                        if (DocType.equals(DocTypeProvider.Passport)) {
                            result = processExtractedTextMRZ(firebaseVisionText);
                            busy[0] = false;
                            if (listener != null && result.length() == 88) {
                                listener.onDataLoaded(image, result);
                            }
                        } else {
                            result = processExtractedText(firebaseVisionText);
                            busy[0] = false;
                        }
                        if (listener != null && isID) {

//                        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//                        toneGen1.startTone(ToneGenerator.TONE_DTMF_0, 150);
                            busy[0] = false;
                            listener.onDataLoaded(image, result);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Do Nothing
                    }
                });
            }
        }
        return "";
    }
}
