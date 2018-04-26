package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.Landmark;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/*
    Activity which handles the live preview the virtual glasses
    including customisation, positioning and rotating as well as layout of views on the screen
*/
public class TryOnActivity extends AppCompatActivity {

    private static final int RC_HANDLE_GMS = 9001;

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

    private ImageView m_screenFlash;
    private Animation m_flash;

    private CameraSource m_camSource = null;

    private CameraPreview m_camPreview;

    private TryOnSurface m_tryOnSurface;

    private TryOnRenderer m_tryOnRenderer;

    private int scaleProgress = 50;

    private FrameData m_frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_on);

        //get and store the frame data
        m_frame = getIntent().getParcelableExtra("Frame");

        //get each of the colours and store them
        m_frontCol = m_frame.GetFrontCol();
        m_leftCol = m_frame.GetLeftCol();
        m_rightCol = m_frame.GetRightCol();
        m_lensCol = m_frame.GetLensCol();

        //get the close button and set it to close the activity once clicked
        m_closeButton = findViewById(R.id.close_button);
        m_closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnToHome();
            }
        });

        //get the surface and renderer of the AR view
        m_tryOnSurface = findViewById(R.id.try_on_surface);
        m_tryOnRenderer = m_tryOnSurface.GetRenderer();

        //set initial values fo the renderer
        m_tryOnRenderer.SetMesh(getApplicationContext(),m_frame.GetMeshID());
        m_tryOnRenderer.SetActivity(this);

        //get the camera preview and link it to the try on surface
        m_camPreview = findViewById(R.id.cam_preview);
        m_camPreview.SetTryOnSurface(m_tryOnSurface);
        m_tryOnSurface.SetCamPreview(m_camPreview);
        createCameraSource();

        m_screenFlash = findViewById(R.id.screen_flash);
        //create an alpha animation to display a flash when an image is taken
        m_flash = new AlphaAnimation(1.0f,0.0f);
        m_flash.setDuration(250);
        m_flash.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                m_screenFlash.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                m_screenFlash.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //setup the views of the customisation part of the activity
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

        //get seekbars and set them to change colour of the selected part
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

        //get the scale seekbar and set it store its progress whenever this is changed
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

        //hide layouts to begin with so that they do not appear until the customise button is pressed
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
        //set the progress of each colour bar to match the colour of the new part
        m_redBar.setProgress((int) (_colour[0]*100));
        m_greenBar.setProgress((int) (_colour[1]*100));
        m_blueBar.setProgress((int) (_colour[2]*100));
        m_opacityBar.setProgress((int) (_colour[3]*100));
    }

    public void GetColours()
    {
        //send the colours of each part to the renderer
        m_tryOnRenderer.SetColours(m_frontCol,m_leftCol,m_rightCol,m_lensCol);
    }

    private void ReturnToHome(){
        //end the activity
        finish();
    }

    private void createCameraSource() {

        Context context = getApplicationContext();

        //create the face detector to ignore classifications, detect all landmarks, use accurate mode,
        //only use the prominent face and track the face
        m_detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(true)
                .setTrackingEnabled(true)
                .build();

        //create a processor to run the detections
        m_detector.setProcessor(new MultiProcessor.Builder<>(new FaceTrackerFactory())
                .build());

        //create the camera preview stream
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
        //resume the camera on re-entering the app
        StartCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop the camera on exiting the app
        m_camPreview.Stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //release the camera, close the renderer, and release the detector on closing the activity
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

        // check that the device has play services available and if not, prompt the user to download the latest version
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        //if the camera source has been built start it
        if(m_camSource != null)
        {
            try{
                m_camPreview.Start(m_camSource);
            }catch (IOException e)
            {
                //if an error occurs while starting it release the camera and set it to null
                m_camSource.release();
                m_camSource = null;
            }
        }
    }

    private void TakePicture(){
        //tell the renderer to save it's surface as a bitmap
        m_tryOnRenderer.ReadyForCapture();
        //disable the capture button so that there is no attempt to capture multiple images at once
        m_captureButton.setClickable(false);
        //tell the camera to take a picture and create the picture callback function
        m_camSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                //create a path to the app's directory
                String path = Environment.getExternalStorageDirectory().toString() + getResources().getString(R.string.folder_name);
                File f = new File(path);
                //if the directory doesn't exist then create it'
                if (!f.exists())
                {
                    f.mkdirs();
                }

                //create a bitmap from the bytes from the camera
                Bitmap picture = BitmapFactory.decodeByteArray(bytes,0, bytes.length);

                //wait for the renderer to have finished capturing its surface
                //this is running on a separate thread so, even though what is inside the while
                //loop does not effect the loop condition, it will not run forever
                while(m_tryOnRenderer.GetCapturing())
                {
                    Log.d("bitmapFactory", "waiting");
                }
                //combine the bitmaps from the camera and the renderer
                Bitmap result = CombineBitmaps(picture,m_tryOnRenderer.GetSurfaceBitmap());
                try{
                    //try to create a filepath with the name of the frame name
                    int iterator = 0;
                    File output = new File(Environment.getExternalStorageDirectory().toString()
                            + getResources().getString(R.string.folder_name)+ "/" + m_frame.GetName() + ".jpg");
                    //if this file already exists add an iterator and increase it until there is not a file with that name
                    while(output.exists()){
                        iterator++;
                        output = new File(Environment.getExternalStorageDirectory().toString()
                                + getResources().getString(R.string.folder_name)+ "/" + m_frame.GetName() + "(" + iterator + ").jpg");
                    }
                    //create the file
                    output.createNewFile();
                    //stream the bitmap data nto the newly created file and compress it to JPEG format
                    FileOutputStream stream = new FileOutputStream(output);
                    result.compress(Bitmap.CompressFormat.JPEG,75,stream);
                    //empty and close the stream
                    stream.flush();
                    stream.close();
                    //allow the user the take another picture
                    m_captureButton.setClickable(true);
                    //flash white across the screen to show that a picture has been taken
                    m_screenFlash.startAnimation(m_flash);
                }catch (IOException e)
                {
                    //if there was an io problem print the stack trace to debug
                    e.printStackTrace();
                }
            }
        });
    }

    private Bitmap CombineBitmaps(Bitmap _back, Bitmap _front){
        int width = _back.getWidth();
        int height = _back.getHeight();
        //create a bitmap the size of the back (camera) image
        Bitmap combination = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        //create rects to define the sizes of both bitmaps
        Rect backRect = new Rect(0,0,width,height);
        Rect frontRect = new Rect(0,0, _front.getWidth(), _front.getHeight());
        //set up a painter which uses the source over porter duff mode for alpha blending
        Paint blendpainter = new Paint();
        blendpainter.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        //create a canvas from the correct sized bitmap to draw the combination to
        Canvas canvas = new Canvas(combination);
        //draw the back (camera) picture
        canvas.drawBitmap(_back,backRect,backRect,null);
        //draw the front (AR Glasses) picture on top with the correct blend mode
        canvas.drawBitmap(_front,frontRect,backRect,blendpainter);
        //return the now drawn on bitmap
        return combination;
    }

    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            //create a new face tracker whenever a face is found
            return new FaceTracker();
        }
    }

    private class FaceTracker extends Tracker<Face> {

        //this function handles what is done on every update in which a face is present
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

                //GetAngleBetween does not determine which direction the angle is in (clockwise/anticlockwise)
                //so rotation is flipped when the left eye is higher than the right eye to determine this
                if(leftEye.getPosition().y > rightEye.getPosition().y)
                {
                    zRot = -zRot;
                }

                //project ToNose vector onto the eye-to-eye vector;
                float rightToNoseProjected = Vector2D.CompOfBOnA(rightToLeft,rightToNose);

                //define a ratio of this projected vector out of the eye-to-eye vector
                float ratio = rightToNoseProjected / rightToLeft.Magnitude();

                //the y rotation (up from the head) is determined by the ratio -0.5 to determine in which direction to rotate
                //and multiplied by 80 degrees to determine where the face is facing in an 80 degree cone
                yRot = (ratio - 0.5f) * 80.0f;

                //occlude arms based on head direction
                boolean left = false;
                boolean right = false;

                //if the y rotation is within a small region where the face is almost straight on
                //neither arm should show as they would be behind the face,
                //if the y rotation is either side of that then one of the arms should be occluded
                //and the other not as only one arm will be behind the head
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
                //create an offset position from the center in the scale -1to1 to match openGL clip space
                //this is then added to then added to the vertices in the vertex shader to move them to the correct position in clip space
                Vector2D offset = new Vector2D(((betweenEyes.x-centerOfImage.x)/centerOfImage.x)*-1,((betweenEyes.y-centerOfImage.y)/centerOfImage.y)*-1);

                //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                //send results to renderer
                m_tryOnRenderer.SetGlassesTransformation(-yRot, zRot,scale,offset);
            }
        }
    }
}