package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Adam on 27/02/2018.
 */

public class TryOnRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "Renderer";

    private Mesh m_mesh;
    private static Shader m_shader = null;

    private final float[] m_VPMatrix = new float[16];
    private final float[] m_projectionMatrix = new float[16];
    private final float[] m_viewMatrix = new float[16];
    private final float[] m_modelMatrix = new float[16];
    private final float[] m_MVPMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        m_mesh = new Mesh();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0,width, height);

        float ratio = (float)width / height;
        Matrix.perspectiveM(m_projectionMatrix,0,90,ratio,0.5f,10);
        Matrix.setLookAtM(m_viewMatrix, 0, 0, 0, 1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(m_VPMatrix, 0, m_projectionMatrix, 0, m_viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //clear colour buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(m_MVPMatrix,0, m_VPMatrix,0, m_modelMatrix,0);
        //Drawing
        m_mesh.Draw(m_MVPMatrix);
    }

    public static Shader GetShader()
    {
        if (m_shader == null)
        {
            Log.d(TAG, "Shader was null");
            m_shader = new Shader();
        }
        return m_shader;
    }

    public static int LoadShader(int _type, String _shader )
    {
        int shaderLoc = GLES20.glCreateShader(_type);

        GLES20.glShaderSource(shaderLoc, _shader);
        GLES20.glCompileShader(shaderLoc);

        return shaderLoc;
    }

    public void SetGlassesRotation(float _y, float _z){
        Matrix.setRotateEulerM(m_modelMatrix,0,0,_y,_z);
    }
}
