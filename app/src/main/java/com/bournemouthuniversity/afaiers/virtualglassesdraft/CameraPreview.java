package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import com.google.android.gms.common.images.Size;

import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

/**
 * Created by Adam on 12/02/2018.
 */

class CameraPreview extends ViewGroup {

    private static final String TAG = "CamPreview";

    private Context m_context;
    private SurfaceView m_surfaceView;
    private boolean m_startRequested;
    private boolean m_surfaceAvailable;
    private CameraSource m_camSource = null;
    private int m_layoutWidth;
    private int m_layoutHeight;
    private TryOnSurface m_tryOnSurface = null;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_context = context;

        m_startRequested = false;
        m_surfaceAvailable = false;

        m_surfaceView = new SurfaceView(context);
        m_surfaceView.getHolder().addCallback(new SurfaceCallback());

        addView(m_surfaceView);
    }

    public void Start(CameraSource camSource) throws IOException {
        if (camSource == null)
            Stop();

        m_camSource = camSource;

        if (m_camSource != null) {
            m_startRequested = true;
            StartIfReady();
        }
    }

    private void StartIfReady() throws IOException {

        if (m_startRequested && m_surfaceAvailable) {
            //TODO: Fix permissions check error
            m_camSource.start(m_surfaceView.getHolder());

            int width = 720;
            int height = 1280;
            if(m_camSource != null)
            {
                Log.d(TAG, "Cam source isn't null");
                Size size = m_camSource.getPreviewSize();
                width = size.getHeight();
                height = size.getWidth();
            }

            float childHeight = m_layoutHeight;
            float childWidth = ((m_layoutHeight/(float)height) * (float) width);

            DisplayMetrics DM = new DisplayMetrics();
            ((TryOnActivity) m_context).getWindowManager().getDefaultDisplay().getMetrics(DM);
            int screenWidthOffset = ((int)childWidth - DM.widthPixels)/2;

            for (int i = 0; i < getChildCount(); i++)
            {
                getChildAt(i).layout( -screenWidthOffset, 0,(int)childWidth-screenWidthOffset,(int)childHeight);
            }
            m_tryOnSurface.layout( -screenWidthOffset, 0,(int)childWidth-screenWidthOffset,(int)childHeight);

            m_startRequested = false;
        }
    }

    public void Stop() {
        m_camSource.stop();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 720;
        int height = 1280;

        m_layoutWidth = right - left;
        m_layoutHeight = bottom - top;

        Log.d(TAG,"layoutwidth: "+m_layoutWidth);
        Log.d(TAG, "layoutHeight: "+m_layoutHeight);

        float childWidth = m_layoutWidth;
        float childHeight = ((m_layoutWidth / (float) width) * (float) height);

        if(childHeight > m_layoutHeight)
        {
            childHeight = m_layoutHeight;
            childWidth = (( m_layoutHeight / (float) height) * (float) width);
        }

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(0, 0, (int) childWidth, (int) childHeight);
        }


        try {
            StartIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }

    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            m_surfaceAvailable = true;
            try {
                StartIfReady();
            }catch (IOException e){
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            m_surfaceAvailable = false;
        }
    }

    public void SetTryOnSurface(TryOnSurface _surface)
    {
        m_tryOnSurface = _surface;
    }

    public Vector2D GetCenterOfCam()
    {
        Size size = m_camSource.getPreviewSize();
        float width = size.getHeight();
        float height = size.getWidth();
        return new Vector2D(width/2,height/2);
    }
}
