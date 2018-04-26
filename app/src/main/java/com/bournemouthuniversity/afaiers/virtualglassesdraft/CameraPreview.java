package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.google.android.gms.common.images.Size;

import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

/*
    Class which holds the camera preview surface on the layout
    also handles pushing the camera preview stream to that surface
 */

class CameraPreview extends ViewGroup {

    private Context m_context;
    private SurfaceView m_surfaceView;
    private boolean m_startRequested;
    private boolean m_surfaceAvailable;
    private CameraSource m_camSource = null;
    private int m_layoutHeight;
    private int m_left;
    private int m_top;
    private int m_right;
    private int m_bottom;
    private TryOnSurface m_tryOnSurface = null;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_context = context;

        m_startRequested = false;
        m_surfaceAvailable = false;

        //generate a new surface view to display the camera preview stream and apply the appropriate callback function
        m_surfaceView = new SurfaceView(context);
        m_surfaceView.getHolder().addCallback(new SurfaceCallback());

        //add the generated view to be a child of this one and initialise its layout
        addView(m_surfaceView);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(0, 0, 1, 1);
        }
    }

    //Public setup and begin function for this class
    public void Start(CameraSource camSource) throws IOException {
        if (camSource == null)
            Stop();

        m_camSource = camSource;

        if (m_camSource != null) {
            m_startRequested = true;
            StartIfReady();
        }
    }

    //called to begin sending the stream to the surface and set the correct size of surface
    private void StartIfReady() throws IOException {
        try {
            if (m_startRequested && m_surfaceAvailable) {
                //start the preview stream and send data to this surface
                m_camSource.start(m_surfaceView.getHolder());

                //default initialisation
                int width = 720;
                int height = 1280;
                if (m_camSource != null) {
                    //get the correct size of the preview stream and store it
                    Size size = m_camSource.getPreviewSize();
                    width = size.getHeight();
                    height = size.getWidth();
                }

                //make the surface fill the screen vertically and scale the width accordingly to maintain aspect ratio
                float childHeight = m_layoutHeight;
                float childWidth = (( m_layoutHeight / (float) height) * (float) width);

                //get the metrics of the screen and define an offset for where to place the left and right of the surface
                //to ensure the camera is centered
                DisplayMetrics DM = new DisplayMetrics();
                ((TryOnActivity) m_context).getWindowManager().getDefaultDisplay().getMetrics(DM);
                int screenWidthOffset = ((int) childWidth - DM.widthPixels) / 2;

                //set up rect for the layout of the surface
                m_left = -screenWidthOffset;
                m_top = 0;
                m_right = (int) childWidth - screenWidthOffset;
                m_bottom = (int)childHeight;

                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).layout(m_left, m_top, m_right, m_bottom);
                }
                //assign the same parameters to the openGL surface so that they correctly align
                m_tryOnSurface.layout(m_left, m_top, m_right, m_bottom);
                m_startRequested = false;
            }
        }
        catch(SecurityException e)
        {
        }
    }

    public void Stop() {
        //end the camera preview stream
        if(m_camSource!=null)
        {
            m_camSource.stop();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        //when layout is called find the height of the screen
        m_layoutHeight = bottom - top;

        try {
            //resize the surface if necessary
            StartIfReady();
        } catch (IOException e) {

        }

    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            //allows the surface to be edited only once it has been created
            m_surfaceAvailable = true;
            try {
                StartIfReady();
            }catch (IOException e){

            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            //stops edits to the surface when it does not exist
            m_surfaceAvailable = false;
        }
    }

    public void SetTryOnSurface(TryOnSurface _surface)
    {
        m_tryOnSurface = _surface;
    }

    //used to find the exact center of the camera which is needed for position calculations for the glasses
    public Vector2D GetCenterOfCam()
    {
        Size size = m_camSource.getPreviewSize();
        float width = size.getHeight();
        float height = size.getWidth();
        return new Vector2D(width/2,height/2);
    }

    //called whenever the openGL layout is changed to ensure that it always remains the same size as the camera preview surface
    public void MatchLayout(int _left, int _top, int _right, int _bottom)
    {
        if(_left != m_left || _top != m_top || _right != m_right || _bottom != m_bottom)
        {
            m_tryOnSurface.layout(m_left, m_top, m_right, m_bottom);
        }
    }
}
