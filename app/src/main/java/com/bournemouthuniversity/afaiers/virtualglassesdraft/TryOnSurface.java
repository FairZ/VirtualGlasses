package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.util.jar.Attributes;

/**
 * Created by Adam on 27/02/2018.
 */

public class TryOnSurface extends GLSurfaceView {

    private final TryOnRenderer m_renderer;
    private CameraPreview m_camPreview = null;

    public TryOnSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        //set open gl context version
        setEGLContextClientVersion(2);

        setEGLConfigChooser(8,8,8,8,16,0);
        setZOrderMediaOverlay(true);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        //create and apply renderer
        m_renderer = new TryOnRenderer();
        setRenderer(m_renderer);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(m_camPreview != null) {
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
