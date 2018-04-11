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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.Landmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TryOnActivity extends AppCompatActivity {

    private static final String TAG = "TryOn";
    private static final int RC_HANDLE_GMS = 9001;
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    //layout setup
    private Button m_captureButton;

    private ImageButton m_closeButton;

    private SeekBar m_scaleBar;

    private CameraSource m_camSource = null;

    private CameraPreview m_camPreview;

    private TryOnSurface m_tryOnSurface;

    private TryOnRenderer m_tryOnRenderer;

    private int scaleProgress = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        //get and setup capture button
        m_captureButton = findViewById(R.id.capture_button);
        assert m_captureButton != null;
        m_captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TakePicture();
            }
        });

        m_closeButton = findViewById(R.id.close_button);
        m_closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnToHome();
            }
        });

        m_scaleBar = findViewById(R.id.scale_bar);
        m_scaleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                scaleProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_tryOnSurface = findViewById(R.id.try_on_surface);
        m_tryOnRenderer = m_tryOnSurface.GetRenderer();

        m_tryOnRenderer.SetMesh(getApplicationContext(),R.raw.test1);

        //get the camera preview
        m_camPreview = findViewById(R.id.cam_preview);
        m_camPreview.SetTryOnSurface(m_tryOnSurface);
        createCameraSource();
    }

    private void ReturnToHome(){
        finish();
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
                .setRequestedPreviewSize(1080, 1920)
                .setRequestedFps(24.0f)
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

    private void TakePicture(){
        m_tryOnRenderer.ReadyForCapture();
        m_camSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                Bitmap picture = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                Bitmap result = CombineBitmaps(picture,m_tryOnRenderer.GetSurfaceBitmap());
                try{
                    int iterator = 0;
                    File output = new File(Environment.getExternalStorageDirectory().toString()
                            + getResources().getString(R.string.folder_name)+ "/Pic_" + GetTime() + "(" + iterator + ").jpg");
                    while(output.exists()){
                        iterator++;
                        output = new File(Environment.getExternalStorageDirectory().toString()
                                + getResources().getString(R.string.folder_name)+ "/Pic_" + GetTime() + "(" + iterator + ").jpg");
                    }
                    output.createNewFile();
                    FileOutputStream stream = new FileOutputStream(output);
                    result.compress(Bitmap.CompressFormat.JPEG,75,stream);
                    stream.flush();
                    stream.close();
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private Bitmap CombineBitmaps(Bitmap _back, Bitmap _front){
        //TODO: MAYBE PUSH TO BACKGROUND THREAD
        int width = _back.getWidth();
        int height = _back.getHeight();
        Bitmap combination = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        Rect backRect = new Rect(0,0,width,height);
        Rect frontRect = new Rect(0,0, _front.getWidth(), _front.getHeight());

        Canvas canvas = new Canvas(combination);
        canvas.drawBitmap(_back,backRect,backRect,null);
        canvas.drawBitmap(_front,frontRect,backRect,null);
        return combination;
    }

    private String GetTime(){
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH-mm_dd-MM-yyyy");
        return sdf.format(cal.getTime());
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

            //check that all landmarks have been found as they are required for the calculations
            if(leftEye != null && rightEye != null && noseBase != null)
            {
                //rotation calculations
                float yRot;
                float zRot;
                //set up a series of vectors from the three positions
                Vector2D rightToLeft = new Vector2D(leftEye.getPosition().x - rightEye.getPosition().x,
                                                    leftEye.getPosition().y - rightEye.getPosition().y);

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

                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //scale calculations

                //sets scale in a range from 0.5 to 1.5
                float scale = ((float)scaleProgress / 100.0f) + 0.5f;

                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //Translation calculations

                //find the point directly between the eyes
                Vector2D betweenEyes = new Vector2D((leftEye.getPosition().x+rightEye.getPosition().x)/2,(leftEye.getPosition().y + rightEye.getPosition().y)/2);
                //find the center point of the image from the camera
                Vector2D centerOfImage = m_camPreview.GetCenterOfCam();
                //create an offset position from the center in the scale -1to1 (to match openGL clip space)
                Vector2D offset = new Vector2D(((betweenEyes.x-centerOfImage.x)/centerOfImage.x)*-1,((betweenEyes.y-centerOfImage.y)/centerOfImage.y)*-1);

                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //send results to renderer
                m_tryOnRenderer.SetGlassesTransformation(-yRot, zRot,scale,offset);
            }
        }
    }
}