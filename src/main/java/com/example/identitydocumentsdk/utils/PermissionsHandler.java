package com.example.identitydocumentsdk.utils;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

//Remember to impelment onRequestPermissionsResult on the parent class to properly handle the permission results

public class PermissionsHandler  {

    private static boolean checkedPermissions = false;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static Activity activity;

    public static boolean handlePermissions(Activity act){
        activity = act;
        if (!checkedPermissions && !allPermissionsGranted()) {
            ActivityCompat.requestPermissions(activity, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
            return false;
        } else {
            checkedPermissions = true;
            return true;
        }
    }

    private static boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    activity
                            .getPackageManager()
                            .getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }
}
