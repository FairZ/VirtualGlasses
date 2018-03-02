package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.renderscript.Float2;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.Landmark;

import java.io.IOException;
import java.util.List;

public class TryOnActivity extends AppCompatActivity {

    private static final String TAG = "TryOn";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //layout setup
    private Button m_captureButton;

    private CameraSource m_camSource = null;

    private CameraPreview m_camPreview;

    private TryOnRenderer m_tryOnRenderer;

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

        TryOnSurface m_tryOnSurface = findViewById(R.id.try_on_surface);
        m_tryOnRenderer = m_tryOnSurface.GetRenderer();
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
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        detector.setProcessor(new MultiProcessor.Builder<>(new FaceTrackerFactory())
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


    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {

            Log.d(TAG, "Found Face!");
            return new FaceTracker();
        }
    }

    private class FaceTracker extends Tracker<Face> {
        @Override
        public void onUpdate(Detector.Detections<Face> detections, Face face) {
            super.onUpdate(detections, face);
            Landmark leftEye = null;
            Landmark rightEye = null;
            Landmark noseBase = null;

            //search through the landmarks and get the appropriate ones
            List<Landmark> landmarks = face.getLandmarks();
            for (Landmark landmark : landmarks)
            {
                switch(landmark.getType())
                {
                    case Landmark.LEFT_EYE:
                        leftEye = landmark;
                        break;
                    case Landmark.RIGHT_EYE:
                        rightEye = landmark;
                        break;
                    case Landmark.NOSE_BASE:
                        noseBase = landmark;
                        break;
                }
            }

            float yRot = 180;
            float zRot = 0;

            //check that all landmarks have been found as they are required for the calculations
            if(leftEye != null && rightEye != null && noseBase != null)
            {
                //set up a series of vectors from the three positions
                Vector2D rightToLeft = new Vector2D(leftEye.getPosition().x - rightEye.getPosition().x,
                                                    leftEye.getPosition().y - rightEye.getPosition().y);

                Vector2D leftToNose = new Vector2D(noseBase.getPosition().x - leftEye.getPosition().x,
                                                noseBase.getPosition().y - leftEye.getPosition().y);

                Vector2D rightToNose = new Vector2D(noseBase.getPosition().x - rightEye.getPosition().x,
                                                noseBase.getPosition().y - rightEye.getPosition().y);

                //rotation in the z axis (towards camera) is equal to the angle between
                //the normalised rightToLeft vector and the x axis
                zRot = Vector2D.GetAngleBetween(rightToLeft.Normalise(), new Vector2D(1,0));

                //GetAngleBetween does not determine which direction the angle is in
                //so rotation is flipped when the left eye is higher than the right eye
                if(leftEye.getPosition().y > rightEye.getPosition().y)
                {
                    zRot = -zRot;
                }

                //project ToNose vectors onto the eye-to-eye vector;
                float rightToNoseProjected = Vector2D.CompOfBOnA(rightToLeft,rightToNose);

                float ratio = rightToNoseProjected / rightToLeft.Magnitude();

                yRot = (ratio - 0.5f) * 90.0f;

            }

            m_tryOnRenderer.SetGlassesRotation(-yRot, zRot);
        }
    }
}