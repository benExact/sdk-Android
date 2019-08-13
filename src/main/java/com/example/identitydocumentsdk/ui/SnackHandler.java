package com.example.identitydocumentsdk.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.identitydocumentsdk.R;

public class SnackHandler {
    public static void showMessage(Context context, String message, int duration){
        Toast t = Toast.makeText(context,message,duration);
        t.getView().setBackgroundResource(R.drawable.snackbar_background_rect);
        TextView v = (TextView) t.getView().findViewById(android.R.id.message);
        v.setWidth(1000);
        v.setTextColor(Color.WHITE);
        v.setPadding(20,30,0,30);
//        t.setGravity(Gravity.BOTTOM, 0, 0);
        t.show();
    }
}
