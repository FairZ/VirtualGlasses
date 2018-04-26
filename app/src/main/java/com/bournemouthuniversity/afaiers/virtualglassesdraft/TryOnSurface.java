package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/*
    GL surface view which handles the layout and openGL context setup of the AR glasses
*/
public class TryOnSurface extends GLSurfaceView {

    private final TryOnRenderer m_renderer;
    private CameraPreview m_camPreview = null;

    public TryOnSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        //set open gl context version
        setEGLContextClientVersion(2);
        //setup colour depth and stencil sizes to allow for transparent background
        setEGLConfigChooser(8,8,8,8,16,0);
        //turns the surface into a simple overlay drawn in the correct order (setup by layout)
        setZOrderMediaOverlay(true);
        //setup pixel format for format to include transparency
        getHolder().setFormat(PixelFormat.RGBA_8888);
        //create and apply renderer
        m_renderer = new TryOnRenderer();
        setRenderer(m_renderer);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(m_camPreview != null) {
            //whenever the layout is changed be sure to match the layout of the surface to that of the
            //camera so that they always align
            m_camPreview.MatchLayout(left, top, right, bottom);
        }
    }

    public TryOnRenderer GetRenderer()
    {
        return m_renderer;
    }

    public void SetCamPreview(CameraPreview _camPreview)
    {
        m_camPreview = _camPreview;
    }
}
