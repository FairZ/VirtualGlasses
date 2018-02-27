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

    public TryOnRenderer GetRenderer()
    {
        return m_renderer;
    }

}
