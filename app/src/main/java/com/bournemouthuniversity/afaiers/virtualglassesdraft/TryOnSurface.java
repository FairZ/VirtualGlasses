package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Adam on 27/02/2018.
 */

public class TryOnSurface extends GLSurfaceView {

    private final TryOnRenderer m_renderer;

    public TryOnSurface(Context context) {
        super(context);
        //set open gl context version
        setEGLContextClientVersion(2);

        //create and apply renderer
        m_renderer = new TryOnRenderer();
        setRenderer(m_renderer);

        //TODO: determine if it is better to do when dirty or continuous
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }
}
