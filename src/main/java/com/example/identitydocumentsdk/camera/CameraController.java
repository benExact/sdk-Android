package com.example.identitydocumentsdk.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CameraController {

    public static Camera.Parameters parameters;
    public static Camera camera;
    private static int deviceHeight;
    public static int deviceHeightDifference;

    @SuppressWarnings("deprecation")
    public static void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        camera.setPreviewCallback(previewCallback);
    }

    private static boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    private static Camera.Size calcOptimalSize(Context context, List<Camera.Size> sizes) {
        List<Camera.Size> bestSizes = new ArrayList<Camera.Size>();
        List<Float> ratioDifferences = new ArrayList<Float>();

        float tolerance = 0.25f;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        deviceHeight = metrics.heightPixels;
        float targetRatio = ((float) metrics.heightPixels / (float) metrics.widthPixels);
        for (Camera.Size size : sizes) {
            float ratio = (float) size.width / (float) size.height;
            float difference = Math.abs(ratio - targetRatio);
            if (difference <= tolerance) {
                bestSizes.add(size);
                ratioDifferences.add(difference);
            }
        }
        int minIndex = ratioDifferences.indexOf(Collections.min(ratioDifferences));
        return sizes.get(minIndex);
    }

    private static Camera.Size fitScreen(Camera.Size size, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int deviceWidth = metrics.widthPixels;
        int deviceHeight = metrics.heightPixels;
        int cameraWidth = size.width;
        int cameraHeight = size.height;
        float cameraRatio = ((float) cameraWidth / (float) cameraHeight);

        int newWidth;
        int newHeight;

        if (deviceWidth > cameraHeight){
            newWidth = deviceWidth;
            return camera.new Size(newWidth, (int)(newWidth * cameraRatio));
        }

//        if (deviceHeight > cameraHeight) {
//            newHeight = deviceHeight;
//        } else {
//            newHeight = cameraHeight;
//        }
//
//        if (cameraRatio > 1){
//            return camera.new Size((int)(newHeight * cameraRatio), newHeight);
//        }

        return size;
    }

    public static void startCameraPreview(FrameLayout fragmentLayout, SurfaceHolder holder, int displayOrientation, int frameRate, Context context) {
        parameters = camera.getParameters();
        camera.setDisplayOrientation(displayOrientation);
        Camera.Size size = calcOptimalSize(context, parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(size.width, size.height);
        size = fitScreen(size,context);

        if (size.height > size.width){
            fragmentLayout.setLayoutParams(new FrameLayout.LayoutParams(size.width, size.height));
            deviceHeightDifference = Math.abs(deviceHeight - size.height);
        }else{
            fragmentLayout.setLayoutParams(new FrameLayout.LayoutParams(size.height, size.width));
            deviceHeightDifference = Math.abs(deviceHeight - size.width);

        }

        parameters.setPreviewFrameRate(frameRate);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        camera.setParameters(parameters);
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static Camera getCameraInstance(Context context) {
        camera = null;
        try {
            if (hasCamera(context)) {
                camera = Camera.open();

            } else {
                throw new Exception("This device does not have a camera");
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return camera;
    }

    public static void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
}
