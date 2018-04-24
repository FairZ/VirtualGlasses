package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.images.Size;

import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

/**
 * Created by Adam on 12/02/2018.
 */

class CameraPreview extends ViewGroup {

    private Context m_context;
    private SurfaceView m_surfaceView;
    private boolean m_startRequested;
    private boolean m_surfaceAvailable;
    private CameraSource m_camSource = null;
    private int m_layoutWidth;
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

        m_surfaceView = new SurfaceView(context);
        m_surfaceView.getHolder().addCallback(new SurfaceCallback());

        addView(m_surfaceView);
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(0, 0, 1, 1);
        }
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

        try {
            if (m_startRequested && m_surfaceAvailable) {
                m_camSource.start(m_surfaceView.getHolder());

                int width = 720;
                int height = 1280;
                if (m_camSource != null) {
                    Size size = m_camSource.getPreviewSize();
                    width = size.getHeight();
                    height = size.getWidth();
                }

                float childHeight = m_layoutHeight;
                float childWidth = (( m_layoutHeight / (float) height) * (float) width);

                DisplayMetrics DM = new DisplayMetrics();
                ((TryOnActivity) m_context).getWindowManager().getDefaultDisplay().getMetrics(DM);
                int screenWidthOffset = ((int) childWidth - DM.widthPixels) / 2;

                m_left = -screenWidthOffset;
                m_top = 0;
                m_right = (int) childWidth - screenWidthOffset;
                m_bottom = (int)childHeight;

                for (int i = 0; i < getChildCount(); i++) {
                    getChildAt(i).layout(m_left, m_top, m_right, m_bottom);
                }
                m_tryOnSurface.layout(m_left, m_top, m_right, m_bottom);
                m_startRequested = false;
            }
        }
        catch(SecurityException e)
        {
        }
    }

    public void Stop() {
        if(m_camSource!=null)
        {
            m_camSource.stop();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        m_layoutWidth = right - left;
        m_layoutHeight = bottom - top;

        try {
            StartIfReady();
        } catch (IOException e) {

        }

    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
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

    public void MatchLayout(int _left, int _top, int _right, int _bottom)
    {
        if(_left != m_left || _top != m_top || _right != m_right || _bottom != m_bottom)
        {
            m_tryOnSurface.layout(m_left, m_top, m_right, m_bottom);
        }
    }
}
