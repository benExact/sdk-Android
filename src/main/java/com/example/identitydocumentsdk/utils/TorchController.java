package com.example.identitydocumentsdk.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.view.View;

import com.example.identitydocumentsdk.R;
import com.example.identitydocumentsdk.camera.CameraController;

import java.io.IOException;

public class TorchController{
    private static Camera camera;
    private static boolean isTorchOn = false;

    public static boolean isTorchOn() {
        return isTorchOn;
    }

    public static boolean checkTorchAvailability(Context context){
        return  context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public static void turnOn(View view){
        view.setBackgroundResource(R.drawable.flashlight);
        camera = CameraController.camera;
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        isTorchOn = true;

    }

    public static void turnOff(View view){
        view.setBackgroundResource(R.drawable.flashlight_off);
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(p);
        isTorchOn = false;

    }
}
