package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Adam on 27/02/2018.
 */

public class Mesh {
    private FloatBuffer m_vertexBuffer;
    private Shader m_shader;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = {   // in counterclockwise order:
            0.0f,  0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f  // bottom right
    };

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Mesh()
    {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                triangleCoords.length * 4);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        m_vertexBuffer = bb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        m_vertexBuffer.put(triangleCoords);
        // set the buffer to read the first coordinate
        m_vertexBuffer.position(0);

        m_shader = TryOnRenderer.GetShader();
    }

    public void Draw(float[] _MVMatrix)
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(m_shader.GetLocation());

        // get handle to vertex shader's vPosition member
        int positionHandle = GLES20.glGetAttribLocation(m_shader.GetLocation(), "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, m_vertexBuffer);

        int MVPHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "MVP");

        GLES20.glUniformMatrix4fv(MVPHandle,1,false,_MVMatrix,0);

        // get handle to fragment shader's vColor member
        int colorHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }
}
