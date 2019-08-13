package com.example.identitydocumentsdk.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.identitydocumentsdk.R;
import com.example.identitydocumentsdk.barcodedetection.BarcodeProcessing;
import com.example.identitydocumentsdk.barcodedetection.BarcodeProcessingInterface;
import com.example.identitydocumentsdk.idcardocr.IdCardProcessing;
import com.example.identitydocumentsdk.idcardocr.IdCardProcessingInterface;
import com.example.identitydocumentsdk.ui.SnackHandler;
import com.example.identitydocumentsdk.utils.DetectionPhase;
import com.example.identitydocumentsdk.utils.DocTypeProvider;
import com.example.identitydocumentsdk.ui.OverlayPainter;
import com.example.identitydocumentsdk.utils.TorchController;

public class CameraPreviewFragment extends Fragment implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    IdCardProcessing ocr;
    BarcodeProcessing barcodeProcessing;

    IDScanActivity idScanActivity;

    DetectionPhase phase;
    String docType;
    FrameLayout fragmentLayout;
    FrameLayout flipView;
    View torchView;

    Bitmap localIdFront;
    String localOcrString;
    String localBarcodeString;

    OverlayPainter overlayPainter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        phase = DetectionPhase.OCR;
        if (null != barcodeProcessing) {
            barcodeProcessing.cancel(true);
        }

        if (null != ocr) {
            ocr.cancel(true);
        }


        flipView = view.findViewById(R.id.flipAnimationView);
        flipView.setVisibility(View.GONE);

        torchView = view.findViewById(R.id.torchView);
        if (TorchController.checkTorchAvailability(getActivity())) {
            torchView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TorchController.isTorchOn()) {
                        TorchController.turnOff(v);
                    } else {
                        TorchController.turnOn(v);
                    }

                }
            });

        } else {
            torchView.setVisibility(View.GONE);
        }


        fragmentLayout = view.findViewById(R.id.cameraFragmentFrameLayout);
        surfaceView = view.findViewById(R.id.cameraSurfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        docType = getActivity().getIntent().getExtras().getString("docType");

        return view;
    }

    public void stopPreview() {
        if (null != camera) {
            camera.stopPreview();
        }
    }

    private void disposeOCR(IdCardProcessing ocr) {
        ocr.setIdentityCardProcessingListener(null);
        ocr.cancel(true);
    }

    private void disposeBarcodeScanning(BarcodeProcessing barcode) {
        barcode.setBarcodeProcessingListener(null);
        barcode.cancel(true);
    }

    private BarcodeProcessing buildBarcode() {
        barcodeProcessing = new BarcodeProcessing(fragmentLayout, getActivity());
        barcodeProcessing.setBarcodeProcessingListener(new BarcodeProcessingInterface() {
            @Override
            public void onDataLoaded(Bitmap idBack, String barcodeString) {
                localBarcodeString = barcodeString;

                if (!docType.equals(DocTypeProvider.IDCard))
                    localIdFront = idBack;

                if (localBarcodeString.trim().contains(localOcrString) || !docType.equals(DocTypeProvider.IDCard)) {
                    idScanActivity.processCapturedData(localIdFront, idBack, barcodeString);
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_DTMF_0, 150);

                    vibratePhone();

                    disposeBarcodeScanning(barcodeProcessing);
                    stopPreview();
                    getActivity().getFragmentManager().popBackStack();
                } else {
                    barcodeProcessing.setDetected(false);
                    SnackHandler.showMessage(getActivity(), "Please use the same card.", Toast.LENGTH_LONG);
                }
            }

            @Override
            public void onFailure(String message) {
                //Do nothing
            }
        });

        return barcodeProcessing;
    }

    private IdCardProcessing buildOcr() {
        ocr = new IdCardProcessing();
        ocr.setIdentityCardProcessingListener(new IdCardProcessingInterface() {
            @Override
            public void onDataLoaded(Bitmap idFront, String ocrString) {
                disposeOCR(ocr);
                if (docType.equals(DocTypeProvider.Passport)) {
                    ocr.cancel(true);
                    idScanActivity.processCapturedData(idFront, idFront, ocrString);
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_DTMF_0, 100);
                    vibratePhone();
                    stopPreview();
                    getActivity().getFragmentManager().popBackStack();
                } else {
                    phase = DetectionPhase.Barcode;
                }
                localOcrString = ocrString;
                localIdFront = idFront;

                if (docType.equals(DocTypeProvider.IDCard)) {
                    ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                    toneGen1.startTone(ToneGenerator.TONE_DTMF_0, 150);
                    vibratePhone();
                    overlayPainter.updateScanTextOnFade("Scan the back");
                    overlayPainter.playFadeAnimation(700, 1000);
                    overlayPainter.playFlipAnimation(flipView, 1000, 700);
                }
                if (docType.equals(DocTypeProvider.Passport) && localOcrString.length() == 88) {
                    idScanActivity.processCapturedData(localIdFront, localIdFront, localOcrString);
                }
            }

            @Override
            public void onFailure(String message) {
                //Do Nothing for now
            }
        });

        return ocr;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        phase = DetectionPhase.OCR;
        camera = CameraController.getCameraInstance(getActivity());
        CameraController.startCameraPreview(fragmentLayout, surfaceHolder, 90, 30, getActivity());
        idScanActivity = (IDScanActivity) getActivity();

        buildOcr();
        buildBarcode();

        class FrameCounter {
            int counter;

            public void setCount(int counter) {
                this.counter = counter;
            }

            public int getCount() {
                return counter;
            }

            public void increment() {
                counter++;
            }
        }

        final FrameCounter counter = new FrameCounter();
        counter.setCount(0);
        CameraController.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, final Camera camera) {
                if (null != camera) {
                    if (counter.getCount() % 2 == 0) {
                        counter.setCount(0);
                        switch (phase) {
                            case OCR: {
                                if (ocr.getStatus() == AsyncTask.Status.PENDING) {
                                    ocr.setImage(data, camera.getParameters());
                                    ocr.setDocType(docType);
                                    ocr.execute();
                                } else if (ocr.getStatus() == AsyncTask.Status.RUNNING) {
                                    ocr.setImage(data, camera.getParameters());
                                }
                                break;
                            }
                            case Barcode: {
                                ocr.cancel(true);
                                if (barcodeProcessing.getStatus() == AsyncTask.Status.PENDING) {
                                    barcodeProcessing.setImageBytes(data, camera.getParameters());
                                    barcodeProcessing.execute(docType);
                                } else if (barcodeProcessing.getStatus() == AsyncTask.Status.RUNNING) {

                                    barcodeProcessing.setImageBytes(data, camera.getParameters());
                                }
                                break;
                            }
                            default:
                                //Do nothing
                                break;
                        }
                    }
                    counter.increment();
                }

            }
        });
        overlayPainter = new OverlayPainter(fragmentLayout, getActivity());
        overlayPainter.drawOverlay(docType, "");

    }

    public final void vibratePhone() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 1000 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(50);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraController.releaseCamera();
        disposeBarcodeScanning(barcodeProcessing);
        disposeOCR(ocr);
    }


    @Override
    public void onPause() {
        super.onPause();
        CameraController.releaseCamera();
        overlayPainter.disposeOverlays();
        disposeBarcodeScanning(barcodeProcessing);
        disposeOCR(ocr);
    }
}
