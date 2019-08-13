package com.example.identitydocumentsdk.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.identitydocumentsdk.R;
import com.example.identitydocumentsdk.camera.CameraController;
import com.example.identitydocumentsdk.utils.DocTypeProvider;

public class OverlayPainter extends ContextWrapper {
    private View topView;
    private View bottomView;
    private View leftView;
    private View rightView;
    private View lineView;

    private TextView scanTextView;

    private Point idCardRatio = new Point(87, 55);
    private Point greenBookRatio = new Point(78, 110);
    private Point passportRatio = new Point(125, 87);

    private FrameLayout containerView;
    private RelativeLayout textContainerLayout;
    private FrameLayout flipView;
    private LinearLayout brandView;

    private String updatedText;
    private float remainingHeight;
    private int width;
    private int height;
    private int deviceHeightDifference;

    private float topOffset;
    private float bottomOffset;
    private float leftOffset;
    private float rightOffset;

    final float strokeWidth = 10;
    final float strokeCorrection = strokeWidth / 2;
    final float lineSize = 40;
    final float widthModifier = 20;
    final int animationDuration = 2000;

    public OverlayPainter(FrameLayout containerView, Context base) {
        super(base);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        this.containerView = containerView;
        this.topView = View.inflate(getApplicationContext(), R.layout.overlay_view, null);
        topView.setLayoutParams(params);
        this.bottomView = View.inflate(getApplicationContext(), R.layout.overlay_view, null);
        bottomView.setLayoutParams(params);
        this.leftView = View.inflate(getApplicationContext(), R.layout.overlay_view, null);
        leftView.setLayoutParams(params);
        this.rightView = View.inflate(getApplicationContext(), R.layout.overlay_view, null);
        rightView.setLayoutParams(params);
        lineView = View.inflate(getApplicationContext(), R.layout.scan_line_view, null);
        lineView.setLayoutParams(params);
        brandView = (LinearLayout) View.inflate(getApplicationContext(), R.layout.brand_footer_layout, null);
        textContainerLayout = (RelativeLayout) View.inflate(getApplicationContext(), R.layout.scan_text_layout, null);
        scanTextView = (textContainerLayout.findViewById(R.id.scanTextView));

        Point dimensions = getDisplayDimensions(getApplicationContext());
        this.width = dimensions.x;
        this.height = dimensions.y;
    }

    public void drawOverlay(String docType, String captureText) {

        String scanText = "";
        switch (docType) {
            case DocTypeProvider.IDCard:
                topOffset = height / calcHeightOffsetModifier(idCardRatio);
                if (captureText.equals("")) {
                    scanText = "Scan front side of document";
                } else {
                    scanText = captureText;
                }
                break;
            case DocTypeProvider.GreenBookID:
                topOffset = height / calcHeightOffsetModifier(greenBookRatio);
                if (captureText.equals("")) {
                    scanText = "Scan the document";
                } else {
                    scanText = captureText;
                }
                break;
            case DocTypeProvider.Passport:
                topOffset = height / calcHeightOffsetModifier(passportRatio);
                if (captureText.equals("")) {
                    scanText = "Scan the document";
                } else {
                    scanText = captureText;
                }
                break;
            default:
                topOffset = height / widthModifier;
                break;
        }

        bottomOffset = height - topOffset;
        rightOffset = width - leftOffset;

        ReticleView reticleView = new ReticleView(getApplicationContext());
        containerView.addView(reticleView);

        placeTopOverlay();
        placeLeftOverlay();
        placeRightOverlay();
        placeBottomOverlay();

        reticleView.bringToFront();

        drawLine();
        drawScanText(scanText);
        drawBranding();
    }

    private void drawBranding(){

        brandView.setPadding(0,0,0,(getNavigationBarHeight(getApplicationContext()) + (CameraController.deviceHeightDifference / 2)));
        brandView.setVerticalGravity(Gravity.BOTTOM);
        containerView.addView(brandView);
    }

    private void drawLine() {
        lineView.setTranslationY(topOffset + 10);
        lineView.setTranslationX(leftOffset);
        lineView.getLayoutParams().width = (int) calcRemainingWidth();
        lineView.getLayoutParams().height = 15;
        lineView.setAlpha(0.7f);
        lineView.bringToFront();

        playScanAnimation();

        containerView.addView(lineView);
    }

    private float calcRemainingWidth() {
        leftOffset = width / widthModifier;
        return width - (leftOffset * 2);
    }

    private float calcHeightOffsetModifier(Point dimensions) {
        float remainingWidth = calcRemainingWidth();
        float idRatio = (float) dimensions.x / (float) dimensions.y;
        remainingHeight = (remainingWidth / idRatio);
        float overlayHeight = ((height - remainingHeight) / 2);
        return height / overlayHeight;
    }

    public void updateScanTextOnFade(String text) {
        updatedText = text;
    }

    public void disposeOverlays() {
        containerView.removeView(lineView);
        containerView.removeView(topView);
        containerView.removeView(bottomView);
        containerView.removeView(leftView);
        containerView.removeView(rightView);
        containerView.removeView(textContainerLayout);
        containerView.removeView(scanTextView);
        containerView.removeView(brandView);
    }

    private void drawScanText(String text) {
        scanTextView.setText(text);
        scanTextView.setTranslationY(topOffset - 130);

        containerView.addView(textContainerLayout);
    }

    private void placeTopOverlay() {
        topView.setTranslationY(0);
        topView.setTranslationX(0);
        topView.getLayoutParams().width = width;
        topView.getLayoutParams().height = Math.round(topOffset);
        //overlay.bringToFront();

        topView.setBackgroundColor(getColorWithAlpha(Color.parseColor("#000000"), 0.4f));
        containerView.addView(topView);
    }

    private void placeBottomOverlay() {
        int remainingHeight = Math.round(height - (topView.getHeight() + leftView.getHeight()));

        bottomView.setTranslationY(bottomOffset);
        bottomView.setTranslationX(0);
        bottomView.getLayoutParams().width = width;
        bottomView.getLayoutParams().height = Math.round(remainingHeight);
        //overlay.bringToFront();

        bottomView.setBackgroundColor(getColorWithAlpha(Color.parseColor("#000000"), 0.4f));
        containerView.addView(bottomView);
    }

    private void placeLeftOverlay() {
        leftView.setTranslationY(topOffset);
        leftView.setTranslationX(0);
        leftView.getLayoutParams().width = Math.round(leftOffset);
        leftView.getLayoutParams().height = Math.round(bottomOffset - topOffset);
        //overlay.bringToFront();

        leftView.setBackgroundColor(getColorWithAlpha(Color.parseColor("#000000"), 0.4f));
        containerView.addView(leftView);
    }

    private void placeRightOverlay() {
        rightView.setTranslationY(topOffset);
        rightView.setTranslationX(rightOffset);
        rightView.getLayoutParams().width = Math.round(leftOffset);
        rightView.getLayoutParams().height = Math.round(bottomOffset - topOffset);

        rightView.setBackgroundColor(getColorWithAlpha(Color.parseColor("#000000"), 0.4f));
        containerView.addView(rightView);
        //overlay.bringToFront();
    }

    private int getColorWithAlpha(int color, float ratio) {
        int newColor = 0;
        int alpha = Math.round(Color.alpha(color) * ratio);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        newColor = Color.argb(alpha, r, g, b);
        return newColor;
    }

    private static Point getDisplayDimensions(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // find out if status bar has already been subtracted from screenHeight
        display.getRealMetrics(metrics);
        int physicalHeight = metrics.heightPixels;
        int statusBarHeight = getStatusBarHeight(context);
        int navigationBarHeight = getNavigationBarHeight(context);
        int heightDelta = physicalHeight - screenHeight;
        if (heightDelta == 0 || heightDelta == navigationBarHeight) {
            screenHeight -= statusBarHeight;
        }

        return new Point(screenWidth, screenHeight);
    }

    private static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    private static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    //Animations
    public void playFadeAnimation(final long fadeDuration, final long waitDuration) {
        final AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(scanTextView, View.ALPHA, 0f);
        fadeOut.setDuration(fadeDuration);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                scanTextView.setText(updatedText);
            }
        });

        ObjectAnimator wait = ObjectAnimator.ofFloat(scanTextView, View.ALPHA, 0f);
        wait.setDuration(waitDuration);

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(scanTextView, View.ALPHA, 1f);
        fadeIn.setDuration(fadeDuration);

        animatorSet.playSequentially(
                fadeOut,
                wait,
                fadeIn
        );
        animatorSet.start();

//        AdditiveAnimator.animate(scanTextView)
//                .setDuration(fadeDuration)
//                .alpha(0)
//                .addEndAction(new AnimationEndListener() {
//                    @Override
//                    public void onAnimationEnd(boolean wasCancelled) {
//                        scanTextView.setText(updatedText);
//                    }
//                })
//
//                .then()
//                .setDuration(waitDuration)
//                .alpha(0)
//
//                .then()
//                .setDuration(fadeDuration)
//                .alpha(1)
//                .start();
    }

    private void playScanAnimation() {
//        AdditiveAnimator.animate(lineView)
//                .setDuration(animationDuration)
//                .translationY(bottomOffset - 25)
//                .setInterpolator(new ScanningInterpolator())
//                .setRepeatCount(Animation.INFINITE)
//                .start();

        ObjectAnimator animation = ObjectAnimator.ofFloat(lineView, "y", bottomOffset - 25);
        animation.setDuration(animationDuration);
        animation.setInterpolator(new ScanningInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.start();

    }

    public void playFlipAnimation(final FrameLayout view, final long duration, final long fadeDuration) {
        int padding = 30;
        this.flipView = view;
        flipView.removeAllViews();
        flipView.addView(View.inflate(getApplicationContext(), R.layout.card_animation_front_layout, null));
        flipView.setTranslationX(leftOffset + padding);
        flipView.setTranslationY(topOffset + padding);
        flipView.getLayoutParams().width = (int) calcRemainingWidth() - (padding * 2);
        flipView.getLayoutParams().height = (int) remainingHeight - (padding * 2);

        flipView.setVisibility(View.VISIBLE);
        flipView.setAlpha(0);
        flipView.setCameraDistance(10000);

        final AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(flipView, View.ALPHA, 1f);
        fadeIn.setDuration(fadeDuration);

        ObjectAnimator rotation90 = ObjectAnimator.ofFloat(flipView, View.ROTATION_Y, -90);
        rotation90.setDuration(duration / 2);
        rotation90.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                flipView.removeAllViews();
                flipView.addView(View.inflate(getApplicationContext(), R.layout.card_animation_back_layout, null));
            }
        });

        ObjectAnimator rotation180 = ObjectAnimator.ofFloat(flipView, View.ROTATION_Y, -180);
        rotation180.setDuration(duration / 2);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(flipView, View.ALPHA, 0f);
        fadeOut.setDuration(fadeDuration);

        ObjectAnimator rotation0 = ObjectAnimator.ofFloat(flipView, View.ROTATION_Y, 0);
        rotation0.setDuration(duration / 2);

        animatorSet.playSequentially(
                fadeIn,
                rotation90,
                rotation180,
                fadeOut,
                rotation0
        );

        animatorSet.start();

//        AdditiveAnimator.animate(flipView)
//                .setDuration(fadeDuration)
//                .alpha(1)
//
//                .then()
//                .setDuration(duration / 2)
//                .rotationY(-90).addEndAction(new AnimationEndListener() {
//            @Override
//            public void onAnimationEnd(boolean wasCancelled) {
//                flipView.removeAllViews();
//                flipView.addView(View.inflate(getApplicationContext(), R.layout.card_animation_back_layout, null));
//            }
//        })
//                .then()
//                .setDuration(duration / 2)
//                .rotationY(-180)
//
//                .then()
//                .setDuration(fadeDuration)
//                .alpha(0)
//
//                .then()
//                .setDuration(1)
//                .rotationY(0)
//                .start();
    }


    public class ReticleView extends View {
        public ReticleView(Context context) {
            super(context);
        }

        public ReticleView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public ReticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public ReticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawCorners(canvas);
        }

        private Canvas drawCorners(Canvas canvas) {
            canvas.drawColor(Color.TRANSPARENT);

            leftOffset = leftOffset + (strokeWidth / 2);
            topOffset = topOffset + (strokeWidth / 2);
            bottomOffset = bottomOffset - (strokeWidth / 2);
            rightOffset = rightOffset - (strokeWidth / 2);
            Paint myPaint = new Paint();
            myPaint.setColor(Color.rgb(255, 255, 255));
            myPaint.setStrokeWidth(strokeWidth);
            myPaint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(leftOffset, topOffset, leftOffset + lineSize, topOffset, myPaint);
            canvas.drawLine(leftOffset, topOffset - strokeCorrection, leftOffset, topOffset + lineSize, myPaint);
            canvas.drawLine(rightOffset, topOffset, rightOffset - lineSize, topOffset, myPaint);
            canvas.drawLine(rightOffset, topOffset - strokeCorrection, rightOffset, topOffset + lineSize, myPaint);
            canvas.drawLine(leftOffset, bottomOffset, leftOffset + lineSize, bottomOffset, myPaint);
            canvas.drawLine(leftOffset, bottomOffset + strokeCorrection, leftOffset, bottomOffset - lineSize, myPaint);
            canvas.drawLine(rightOffset, bottomOffset, rightOffset - lineSize, bottomOffset, myPaint);
            canvas.drawLine(rightOffset, bottomOffset + strokeCorrection, rightOffset, bottomOffset - lineSize, myPaint);
            return canvas;
        }
    }
}
