package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.util.AttributeSet;
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
    private int layoutWidth;
    private int layoutHeight;

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

            float childHeight = layoutHeight;
            float childWidth = (( layoutHeight / (float) height) * (float) width);
            
            for (int i = 0; i < getChildCount(); i++)
            {
                getChildAt(i).layout(0,0,(int)childWidth,(int)childHeight);
            }

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

        layoutWidth = right - left;
        layoutHeight = bottom - top;

        float childWidth = layoutWidth;
        float childHeight = ((layoutWidth / (float) width) * (float) height);

        if(childHeight > layoutHeight)
        {
            childHeight = layoutHeight;
            childWidth = (( layoutHeight / (float) height) * (float) width);
        }

        for (int i = 0; i < getChildCount(); i++)
        {
            getChildAt(i).layout(0,0,(int)childWidth,(int)childHeight);
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
}
