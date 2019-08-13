package com.example.identitydocumentsdk.facialdetection;

import android.graphics.Bitmap;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.example.identitydocumentsdk.utils.ImageEdit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import java.io.IOException;
import java.util.List;

public class FacialProcessing   {

    private FacialProcessingInterface listener;

    public void setFacialProcessingListener(FacialProcessingInterface listener) {
        this.listener = listener;
    }


    private Bitmap croppedFace;

    public Bitmap getCroppedFace() {
        return croppedFace;
    }


    public void cropFace(Bitmap src, String doc) {
        final String docType = doc;
        final Bitmap image = ImageEdit.getResizedBitmap(src, 1000); //ImageEdit.getBitmapFromPath(src); //ImageEdit.getResizedBitmap(ImageEdit.getBitmapFromPath(src), 600);

        final FirebaseVisionFaceDetectorOptions detectionOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST).build();

        FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(image);
        FirebaseVisionFaceDetector faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(detectionOptions);

        faceDetector.detectInImage(visionImage).addOnSuccessListener(
                new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> faces) {
                        //uses first face thats detected
                        //Toast.makeText(getApplicationContext(),"No faces detected",Toast.LENGTH_LONG).show();
                        if (faces.size() >= 1) {
                            float rotZ = faces.get(0).getHeadEulerAngleZ();
                            final Bitmap rotatedImage = ImageEdit.rotateBitmap(image, rotZ);

                            FirebaseVisionImage rotatedVisionImage = FirebaseVisionImage.fromBitmap(rotatedImage);
                            FirebaseVisionFaceDetector rotatedFaceDetector = FirebaseVision.getInstance().getVisionFaceDetector(detectionOptions);

                            rotatedFaceDetector.detectInImage(rotatedVisionImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                                @Override
                                public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                                    Rect rotatedBounds = firebaseVisionFaces.get(0).getBoundingBox();
                                    croppedFace = Bitmap.createBitmap(rotatedImage, rotatedBounds.left, rotatedBounds.top, rotatedBounds.width(), rotatedBounds.height());

                                    Bitmap srcImage;
                                    srcImage = ImageEdit.extractCard(rotatedImage, rotatedBounds, docType);

                                    listener.onDataLoaded(srcImage, croppedFace);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    listener.onFailure("Something went wrong when detecting face");
                                }
                            });
                        } else {
                            listener.onFailure("No face detected.");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                listener.onFailure("Something went wrong when detecting face");
                //Toast.makeText(getApplicationContext(),"Unable to detect face.",Toast.LENGTH_LONG).show();

            }
        });
    }

}
