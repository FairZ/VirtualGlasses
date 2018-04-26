package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/*
    OpenGL renderer which handles most openGL setup and calls
    also captures the openGL frames and takes inputs from other classes to allow for customisation
    and other drawing options
*/
public class TryOnRenderer implements GLSurfaceView.Renderer {

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

    private TryOnActivity m_activity;

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //set the clear colour, note the alpha is 0.0f to clear to transparency
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //enable required openGL functions
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glEnable( GLES20.GL_DEPTH_TEST );
        GLES20.glDepthFunc( GLES20.GL_LEQUAL );
        GLES20.glDepthMask( true );
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        //initialise the model matrix to an identity matrix
        Matrix.setIdentityM(m_modelMatrix,0);
        //create and store the mesh
        m_mesh = new Mesh(m_context, m_meshResourceID);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        //set the viewport to the correct size to avoid stretching
        GLES20.glViewport(0,0,width, height);
        //set the width and height parameters
        m_width = width;
        m_height = height;
        //determine the aspect ratio
        float ratio = (float)width / height;
        //set up the projection matrix
        Matrix.perspectiveM(m_projectionMatrix,0,54,ratio,0.1f,1);
        //the view matrix
        Matrix.setLookAtM(m_viewMatrix, 0, 0f, 0f, 0.5f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //and their combination
        Matrix.multiplyMM(m_VPMatrix, 0, m_projectionMatrix, 0, m_viewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        //update the colours of each part of the glasses
        m_activity.GetColours();
        //clear colour and depth buffer
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
        //create the MVP matrix
        Matrix.multiplyMM(m_MVPMatrix,0, m_VPMatrix,0, m_modelMatrix,0);
        //Draw the mesh
        m_mesh.Draw(m_MVPMatrix);
        //if a picture is being taken capture the screen once drawing is done
        if(m_capture)
        {
            CaptureSurface();
            //once the surface has been captured reset to false to avoid capturing each frame
            m_capture = false;
        }
    }

    public boolean GetCapturing()
    {
        return m_capture;
    }

    public static Shader GetShader()
    {
        //if a shader does not exist create one
        if (m_shader == null)
        {
            m_shader = new Shader();
        }
        return m_shader;
    }

    public void SetGlassesTransformation(float _yRot, float _zRot, float _scale, Vector2D _translation){
        //first set z rotation
        Matrix.setRotateEulerM(m_modelMatrix,0,0,0,_zRot);
        //then rotate by y rotation
        Matrix.rotateM(m_modelMatrix,0,_yRot,0,1,0);
        //then scale
        Matrix.scaleM(m_modelMatrix,0,_scale,_scale,_scale);
        //finally push position to mesh
        m_mesh.SetFramePosition(_translation);
    }

    public void SetMesh(Context _context, int _resourceID){
        m_context = _context;
        m_meshResourceID = _resourceID;
    }

    public void SetActivity(TryOnActivity _activity)
    {
        m_activity = _activity;
    }

    public void ReadyForCapture(){
        m_capture = true;
    }

    private void CaptureSurface(){
        //create a bytebuffer of the correct size
        ByteBuffer buffer = ByteBuffer.allocateDirect(m_width*m_height*4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        //get the pixels of the gl surface
        //NB: this function returns non-premultiplied colours (RGB*A) but android drawing requires
        //premultiplied colours or overflows occur. To solve this, the colours of the fragment's colours
        //are multiplied by the alpha in the fragment shader.
        GLES20.glReadPixels(0,0,m_width,m_height,GLES20.GL_RGBA,GLES20.GL_UNSIGNED_BYTE,buffer);
        //reset the buffer to the beginning
        buffer.rewind();
        //if a bitmap already exists delete it
        if(m_bmp != null)
        {
            m_bmp.recycle();
            m_bmp = null;
        }
        //create a bitmap of the correct size
        m_bmp = Bitmap.createBitmap(m_width, m_height,Bitmap.Config.ARGB_8888);
        //push the data to the bitmap
        m_bmp.copyPixelsFromBuffer(buffer);
        //pixels are given upside down (due to openGL's coord system) so the bitmap must be rotated
        android.graphics.Matrix rotation = new android.graphics.Matrix();
        rotation.postRotate(180);
        m_bmp = Bitmap.createBitmap(m_bmp,0,0,m_width,m_height,rotation,true);
    }

    public Bitmap GetSurfaceBitmap(){
        return m_bmp;
    }

    public void SetColours(float[] _front, float[] _left, float[] _right, float[] _lens){
        m_mesh.SetFrontColour(_front[0],_front[1],_front[2],_front[3]);
        m_mesh.SetLeftArmColour(_left[0],_left[1],_left[2],_left[3]);
        m_mesh.SetRightArmColour(_right[0],_right[1],_right[2],_right[3]);
        m_mesh.SetLensColour(_lens[0],_lens[1],_lens[2],_lens[3]);
    }

    public void OccludeArm(boolean _left, boolean _right)
    {
        m_mesh.SetOccludes(_left,_right);
    }

    public void Close()
    {
        m_shader = null;
    }
}
