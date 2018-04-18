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
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Visibility;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

    private FaceDetector m_detector;

    private boolean m_customising = false;
    private int m_selectedPart = 0;

    private float[] m_frontCol;
    private float[] m_leftCol;
    private float[] m_rightCol;
    private float[] m_lensCol;

    //layout setup
    private Button m_captureButton;
    private Button m_frontButton;
    private Button m_leftButton;
    private Button m_rightButton;
    private Button m_lensButton;

    private ImageButton m_closeButton;
    private ImageButton m_customiseButton;

    private SeekBar m_scaleBar;
    private SeekBar m_redBar;
    private SeekBar m_blueBar;
    private SeekBar m_greenBar;
    private SeekBar m_opacityBar;

    private LinearLayout m_buttonBar;
    private LinearLayout m_scaleHolder;
    private LinearLayout m_colourHolder;

    private CameraSource m_camSource = null;

    private CameraPreview m_camPreview;

    private TryOnSurface m_tryOnSurface;

    private TryOnRenderer m_tryOnRenderer;

    private int scaleProgress = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_try_on);

        FrameData Frame = getIntent().getParcelableExtra("Frame");

        m_frontCol = Frame.GetFrontCol();
        m_leftCol = Frame.GetLeftCol();
        m_rightCol = Frame.GetRightCol();
        m_lensCol = Frame.GetLensCol();

        m_closeButton = findViewById(R.id.close_button);
        m_closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnToHome();
            }
        });

        m_tryOnSurface = findViewById(R.id.try_on_surface);
        m_tryOnRenderer = m_tryOnSurface.GetRenderer();

        m_tryOnRenderer.SetMesh(getApplicationContext(),Frame.GetMeshID());
        m_tryOnRenderer.SetActivity(this);

        //get the camera preview
        m_camPreview = findViewById(R.id.cam_preview);
        m_camPreview.SetTryOnSurface(m_tryOnSurface);
        m_tryOnSurface.SetCamPreview(m_camPreview);
        createCameraSource();

        SetupCustomisationViews();
    }

    private void SetupCustomisationViews() {
        //get and setup capture button
        m_captureButton = findViewById(R.id.capture_button);
        assert m_captureButton != null;
        m_captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TakePicture();
            }
        });

        //get customisation buttons and assign functions to them
        m_frontButton = findViewById(R.id.front_button);
        m_frontButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleColours(1);
            }
        });

        m_leftButton = findViewById(R.id.left_arm_button);
        m_leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleColours(2);
            }
        });

        m_rightButton = findViewById(R.id.right_arm_button);
        m_rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleColours(3);
            }
        });

        m_lensButton = findViewById(R.id.lenses_button);
        m_lensButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleColours(4);
            }
        });

        //get customise button and assign function for toggling UI to it
        m_customiseButton = findViewById(R.id.customise_button);
        m_customiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToggleCustomisation();
            }
        });

        m_redBar = findViewById(R.id.red_bar);
        m_redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                switch(m_selectedPart)
                {
                    case 1:
                        m_frontCol[0] = (float)progress/100.0f;
                        break;
                    case 2:
                        m_leftCol[0] = (float)progress/100.0f;
                        break;
                    case 3:
                        m_rightCol[0] = (float)progress/100.0f;
                        break;
                    case 4:
                        m_lensCol[0] = (float)progress/100.0f;
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_greenBar = findViewById(R.id.green_bar);
        m_greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                switch(m_selectedPart)
                {
                    case 1:
                        m_frontCol[1] = (float)progress/100.0f;
                        break;
                    case 2:
                        m_leftCol[1] = (float)progress/100.0f;
                        break;
                    case 3:
                        m_rightCol[1] = (float)progress/100.0f;
                        break;
                    case 4:
                        m_lensCol[1] = (float)progress/100.0f;
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_blueBar = findViewById(R.id.blue_bar);
        m_blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                switch(m_selectedPart)
                {
                    case 1:
                        m_frontCol[2] = (float)progress/100.0f;
                        break;
                    case 2:
                        m_leftCol[2] = (float)progress/100.0f;
                        break;
                    case 3:
                        m_rightCol[2] = (float)progress/100.0f;
                        break;
                    case 4:
                        m_lensCol[2] = (float)progress/100.0f;
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        m_opacityBar = findViewById(R.id.opacity_bar);
        m_opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                switch(m_selectedPart)
                {
                    case 1:
                        m_frontCol[3] = (float)progress/100.0f;
                        break;
                    case 2:
                        m_leftCol[3] = (float)progress/100.0f;
                        break;
                    case 3:
                        m_rightCol[3] = (float)progress/100.0f;
                        break;
                    case 4:
                        m_lensCol[3] = (float)progress/100.0f;
                        break;
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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

        //get ui layouts to be able to change visibility
        m_buttonBar = findViewById(R.id.selection_bar);

        m_scaleHolder = findViewById(R.id.scale_layout);

        m_colourHolder = findViewById(R.id.colour_holder);

        m_scaleHolder.setVisibility(View.GONE);
        m_colourHolder.setVisibility(View.GONE);
        m_buttonBar.setVisibility(View.GONE);
    }

    private void ToggleCustomisation() {
        if(!m_customising)
        {
            //if you weren't previously customising, show the UI elements and hide the capture button
            m_buttonBar.setVisibility(View.VISIBLE);
            m_scaleHolder.setVisibility(View.VISIBLE);
            m_captureButton.setVisibility(View.GONE);
            m_closeButton.setVisibility(View.GONE);
            m_customising = true;
        }
        else
        {
            //if you were customising, close the UI elements, reset them and reshow the capture button
            m_buttonBar.setVisibility(View.GONE);
            m_scaleHolder.setVisibility(View.GONE);
            m_colourHolder.setVisibility(View.GONE);
            m_captureButton.setVisibility(View.VISIBLE);
            m_closeButton.setVisibility(View.VISIBLE);
            m_customising = false;
            m_selectedPart = 0;
            m_frontButton.setAlpha(1.0f);
            m_leftButton.setAlpha(1.0f);
            m_rightButton.setAlpha(1.0f);
            m_lensButton.setAlpha(1.0f);
        }
    }

    private void ToggleColours(int _part)
    {
        if (_part == m_selectedPart)
        {
            //if the user taps the activated option then hide the options and reset the button
            m_colourHolder.setVisibility(View.GONE);
            switch(m_selectedPart)
            {
                case 1:
                    SetColourBars(m_frontCol);
                    m_frontButton.setAlpha(1.0f);
                    break;
                case 2:
                    SetColourBars(m_leftCol);
                    m_leftButton.setAlpha(1.0f);
                    break;
                case 3:
                    SetColourBars(m_rightCol);
                    m_rightButton.setAlpha(1.0f);
                    break;
                case 4:
                    SetColourBars(m_lensCol);
                    m_lensButton.setAlpha(1.0f);
                    break;
            }
            m_selectedPart = 0;
        }
        else
        {
            //if a button is pressed switch to it's state
            m_colourHolder.setVisibility(View.VISIBLE);
            m_selectedPart = _part;
            switch(m_selectedPart)
            {
                case 1:
                    SetColourBars(m_frontCol);
                    m_frontButton.setAlpha(0.5f);
                    m_leftButton.setAlpha(1.0f);
                    m_rightButton.setAlpha(1.0f);
                    m_lensButton.setAlpha(1.0f);
                    break;
                case 2:
                    SetColourBars(m_leftCol);
                    m_frontButton.setAlpha(1.0f);
                    m_leftButton.setAlpha(0.5f);
                    m_rightButton.setAlpha(1.0f);
                    m_lensButton.setAlpha(1.0f);
                    break;
                case 3:
                    SetColourBars(m_rightCol);
                    m_frontButton.setAlpha(1.0f);
                    m_leftButton.setAlpha(1.0f);
                    m_rightButton.setAlpha(0.5f);
                    m_lensButton.setAlpha(1.0f);
                    break;
                case 4:
                    SetColourBars(m_lensCol);
                    m_frontButton.setAlpha(1.0f);
                    m_leftButton.setAlpha(1.0f);
                    m_rightButton.setAlpha(1.0f);
                    m_lensButton.setAlpha(0.5f);
                    break;
            }
        }
    }

    private void SetColourBars(float[] _colour)
    {
        m_redBar.setProgress((int) (_colour[0]*100));
        m_greenBar.setProgress((int) (_colour[1]*100));
        m_blueBar.setProgress((int) (_colour[2]*100));
        m_opacityBar.setProgress((int) (_colour[3]*100));
    }

    public void GetColours()
    {
        m_tryOnRenderer.SetColours(m_frontCol,m_leftCol,m_rightCol,m_lensCol);
    }

    private void ReturnToHome(){
        finish();
    }

    private void createCameraSource() {

        Context context = getApplicationContext();

        m_detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        m_detector.setProcessor(new MultiProcessor.Builder<>(new FaceTrackerFactory())
                .build());

        if(!m_detector.isOperational())
        {
            Log.d(TAG, "FaceDetector dependencies have not yet been downloaded");
        }

        m_camSource = new CameraSource.Builder(context, m_detector)
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
        if(m_tryOnRenderer != null)
        {
            m_tryOnRenderer.Close();
        }
        if(m_detector != null)
        {
            m_detector.release();
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
        m_captureButton.setClickable(false);
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
                    m_captureButton.setClickable(true);
                }catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private Bitmap CombineBitmaps(Bitmap _back, Bitmap _front){
        int width = _back.getWidth();
        int height = _back.getHeight();
        Bitmap combination = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        Rect backRect = new Rect(0,0,width,height);
        Rect frontRect = new Rect(0,0, _front.getWidth(), _front.getHeight());

        Paint blendpainter = new Paint();
        blendpainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        Canvas canvas = new Canvas(combination);
        canvas.drawBitmap(_back,backRect,backRect,null);
        canvas.drawBitmap(_front,frontRect,backRect,blendpainter);
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

                yRot = (ratio - 0.5f) * 80.0f;

                //occlude arms based on head direction
                boolean left = false;
                boolean right = false;

                if(yRot > -10.0f)
                    left = true;
                if(yRot < 10.0f)
                    right = true;

                m_tryOnRenderer.OccludeArm(left,right);

                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //scale calculations

                //sets scale in a range from 0.5 to 1.5
                float scale = ((float)scaleProgress / 100.0f) + 0.5f;

                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //Translation calculations

                //find the point directly between the eyes
                Vector2D betweenEyes = new Vector2D(rightEye.getPosition().x+(ratio*rightToLeft.x),rightEye.getPosition().y+(ratio*rightToLeft.y));
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