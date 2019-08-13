package com.example.identitydocumentsdk.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

//import com.documentprocessing.Utils.DocTypeProvider;
//import com.documentprocessing.Utils.SingletonCapturedDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageEdit {
    static String TAG = "IMAGE EDIT";

    public static String getUniqueImageName(){
        long now = System.currentTimeMillis();
        return  now + ".png";
    }

    private static Bitmap cropImage(double leftOffset, double topOffset, double widthOffset, double heightOffset, Bitmap bitmap, Rect facialBounds){
        int x =(int)(facialBounds.left - (facialBounds.width() * leftOffset));
        x = x<=0 ? 0 : x;
        x = x>=bitmap.getWidth() ? bitmap.getWidth() : x;

        int y =(int)(facialBounds.top - (facialBounds.height() * topOffset));
        y = y<=0 ? 0 : y;
        y = y>=bitmap.getHeight() ? bitmap.getHeight() : y;

        int width= (int)(facialBounds.width() * widthOffset);
        width = width<=0 ? 0 : width;
        width = (x + width) > bitmap.getWidth() ? (bitmap.getWidth() - x): width;

        int height=(int)(facialBounds.height() * heightOffset);
        height = height<=0 ? 0 : height;
        height = (y + height) > bitmap.getHeight() ? (bitmap.getHeight() - y): height;

        return Bitmap.createBitmap(bitmap, x, y, width, height);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public static Bitmap extractCard(Bitmap bitmap, Rect facialBounds, String docType){

        if (null != docType) {
            double LEFT_OFFSET = 0;
            double TOP_OFFSET = 0;
            double WIDTH_OFFSET = 0;
            double HEIGHT_OFFSET = 0;

            switch (docType) {
                case DocTypeProvider.IDCard:
                    LEFT_OFFSET = 3.48;
                    TOP_OFFSET = 0.95;
                    WIDTH_OFFSET = 5.07;
                    HEIGHT_OFFSET = 3.09;
                    break;
                case DocTypeProvider.Passport:
                    LEFT_OFFSET = 0.55;
                    TOP_OFFSET = 0.81;
                    WIDTH_OFFSET = 5.6;
                    HEIGHT_OFFSET = 3.71;
                    break;
                case DocTypeProvider.GreenBookID:
                    LEFT_OFFSET = 0.8;
                    TOP_OFFSET = 3.63;
                    WIDTH_OFFSET = 3.8;
                    HEIGHT_OFFSET = 5.09;
                    break;
            }
            try {
                //Bitmap bitmap =  getBitmapFromPath(path);
                Log.d(TAG, bitmap.getWidth() + "x" + bitmap.getHeight() + "==========================================================================");
                Log.d(TAG, facialBounds.left + " " + facialBounds.top + " " + facialBounds.width() + " " + facialBounds.height() + "==========================================================================");

                bitmap = cropImage(LEFT_OFFSET, TOP_OFFSET, WIDTH_OFFSET, HEIGHT_OFFSET, bitmap, facialBounds);
//                String imagePath = saveImage(bitmapToByteArray(bitmap), ImageEdit.getUniqueImageName()).getPath();
                return bitmap;
            }catch (Exception e){
                return bitmap;
            }

        }else{
            return bitmap;
        }
    }

    public static Bitmap frameBytesToBitmap(byte[] data, Camera.Parameters parameters){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;

        YuvImage yuvImage = new YuvImage(data, parameters.getPreviewFormat(), width, height, null);
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, out);

        byte[] imageBytes = out.toByteArray();
        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        return rotateBitmap(image, 90);
    }

    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }

    public static byte[] getResizedByteArray(byte[] byteArray, int maxSize) {
        Bitmap bitmap = byteArrayToBitmap(byteArray);
        Bitmap resized = getResizedBitmap(bitmap, maxSize);
        byte[] resizedByteArray = bitmapToByteArray(resized);

        return resizedByteArray;
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap getBitmapFromPath(String path) {
        try {
            Bitmap bitmap=null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }}

    public static byte[] bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmap.recycle();
        return byteArray;
    }

    public static File saveImage(byte[] bytes, String title) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory()+ "/"+title);

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        }
        catch(Exception e){
            int i = 1;
        }
        finally {
            if (null != output) {
                output.close();
            }

            if (file.exists()) {
                return file;
            }else {
                return new File("josdefina");
            }
        }
    }
}
