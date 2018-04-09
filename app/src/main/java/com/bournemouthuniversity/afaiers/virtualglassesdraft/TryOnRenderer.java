package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Adam on 27/02/2018.
 */

public class TryOnRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "Renderer";

    private int m_meshResourceID;
    private Context m_context;

    private Mesh m_mesh;
    private static Shader m_shader = null;

    private final float[] m_VPMatrix = new float[16];
    private final float[] m_projectionMatrix = new float[16];
    private final float[] m_viewMatrix = new float[16];
    private final float[] m_modelMatrix = new float[16];
    private final float[] m_MVPMatrix = new float[16];

    private Bitmap m_bmp = null;
    private boolean m_capture = false;
    private int m_width = 0;
    private int m_height = 0;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        Matrix.setIdentityM(m_modelMatrix,0);
        m_mesh = new Mesh(m_context, m_meshResourceID);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0,0,width, height);
        m_width = width;
        m_height = height;
        float ratio = (float)width / height;
        Matrix.perspectiveM(m_projectionMatrix,0,90,ratio,0.1f,10);
        Matrix.setLookAtM(m_viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(m_VPMatrix, 0, m_projectionMatrix, 0, m_viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //clear colour buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        Matrix.multiplyMM(m_MVPMatrix,0, m_VPMatrix,0, m_modelMatrix,0);
        //Drawing
        m_mesh.Draw(m_MVPMatrix);
        if(m_capture)
        {
            CaptureSurface();
            m_capture = false;
        }
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

    public void SetGlassesTransformation(float _yRot, float _zRot, float _scale, Vector2D _translation){
        Matrix.setRotateEulerM(m_modelMatrix,0,0,0,_zRot);
        Matrix.rotateM(m_modelMatrix,0,_yRot,0,1,0);
        Matrix.scaleM(m_modelMatrix,0,_scale,_scale,_scale);
        m_mesh.SetFramePosition(_translation);
    }

    public void SetMesh(Context _context, int _resourceID){
        m_context = _context;
        m_meshResourceID = _resourceID;
    }

    public void ReadyForCapture(){
        m_capture = true;
    }

    private void CaptureSurface(){
        ByteBuffer buffer = ByteBuffer.allocateDirect(m_width*m_height*4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0,0,m_width,m_height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buffer);
        buffer.rewind();
        if(m_bmp != null)
        {
            m_bmp.recycle();
            m_bmp = null;
        }
        m_bmp = Bitmap.createBitmap(m_width, m_height,Bitmap.Config.ARGB_8888);
        m_bmp.copyPixelsFromBuffer(buffer);
        android.graphics.Matrix rotation = new android.graphics.Matrix();
        rotation.postRotate(180);
        m_bmp = Bitmap.createBitmap(m_bmp,0,0,m_width,m_height,rotation,true);
    }

    public Bitmap GetSurfaceBitmap(){
        return m_bmp;
    }

}
