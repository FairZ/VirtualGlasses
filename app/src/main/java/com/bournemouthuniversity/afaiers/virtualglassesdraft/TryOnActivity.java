package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class TryOnActivity extends AppCompatActivity {

    private static final String TAG = "TryOn";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //layout setup
    private Button m_captureButton;

    private CameraSource m_camSource = null;

    private CameraPreview m_camPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        //get and setup capture button
        m_captureButton = findViewById(R.id.capture_button);
        assert m_captureButton != null;

        //get the camera preview
        m_camPreview = findViewById(R.id.cam_preview);

        createCameraSource();

    }

    private void createCameraSource() {

        int permissionCheck = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Camera permission is not granted. Requesting permission");

            final String[] permissions = new String[]{Manifest.permission.CAMERA};

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
                return;
            }
        }

        Context context = getApplicationContext();

        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        detector.setProcessor(new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                .build());

        if(!detector.isOperational())
        {
            Log.d(TAG, "FaceDetector dependencies have not yet been downloaded");
        }

        m_camSource = new CameraSource.Builder(context, detector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedPreviewSize(1920, 1080)
                .setRequestedFps(30.0f)
                .build();

        StartCameraSource();
    }

    @Override
    protected void onResume() {
        super.onResume();
        StartCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_camPreview.Stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m_camSource != null)
        {
            m_camSource.release();
        }
    }

    private void StartCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Log.d(TAG, "don't have google play services");
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if(m_camSource != null)
        {
            try{
                m_camPreview.Start(m_camSource);
            }catch (IOException e)
            {
                Log.e(TAG, "Unable to start camera source.", e);
                m_camSource.release();
                m_camSource = null;
            }
        }
    }


    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            //TODO: make something happen when a new face is found
            Log.d(TAG, "Found Face!");
            return null;
        }
    }
}