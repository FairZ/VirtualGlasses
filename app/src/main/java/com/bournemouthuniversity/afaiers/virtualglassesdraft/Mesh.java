package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Adam on 27/02/2018.
 */

public class Mesh {
    private FloatBuffer m_vertexBuffer;
    private ShortBuffer m_faceBuffer;
    private Shader m_shader;
    private List<String> m_vertexList;
    private List<String> m_faceList;
    private Vector2D m_frameOffset;

    public Mesh(Context _context, int _id)
    {
        m_vertexList = new ArrayList<>();
        m_faceList = new ArrayList<>();
        m_frameOffset = new Vector2D();

        Scanner scanner = new Scanner(_context.getResources().openRawResource(_id));
        while(scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if(line.startsWith("v "))
            {
                m_vertexList.add(line);
            }
            else if(line.startsWith("f "))
            {
                m_faceList.add(line);
            }
        }
        scanner.close();

        ByteBuffer forVertices = ByteBuffer.allocateDirect(m_vertexList.size()*3*4);
        forVertices.order(ByteOrder.nativeOrder());
        m_vertexBuffer = forVertices.asFloatBuffer();

        ByteBuffer forFaces = ByteBuffer.allocateDirect(m_faceList.size()*3*2);
        forFaces.order(ByteOrder.nativeOrder());
        m_faceBuffer = forFaces.asShortBuffer();

        for(String vertex:m_vertexList){
            String xyz[] = vertex.split(" ");
            m_vertexBuffer.put(Float.parseFloat(xyz[1]));
            m_vertexBuffer.put(Float.parseFloat(xyz[2]));
            m_vertexBuffer.put(Float.parseFloat(xyz[3]));
        }
        m_vertexBuffer.position(0);

        for(String face: m_faceList){
            String indices[] = face.split(" ");
            String set0[] = indices[1].split("/");
            short index0 = Short.parseShort(set0[0]);
            String set1[] = indices[2].split("/");
            short index1 = Short.parseShort(set1[0]);
            String set2[] = indices[3].split("/");
            short index2 = Short.parseShort(set2[0]);
            m_faceBuffer.put((short)(index0-1));
            m_faceBuffer.put((short)(index1-1));
            m_faceBuffer.put((short)(index2-1));
        }
        m_faceBuffer.position(0);

        m_shader = TryOnRenderer.GetShader();
    }

    public void Draw(float[] _MVPMatrix)
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(m_shader.GetLocation());

        // get handle to vertex shader's vPosition member
        int positionHandle = GLES20.glGetAttribLocation(m_shader.GetLocation(), "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                3*4, m_vertexBuffer);

        int MVPHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "MVP");

        GLES20.glUniformMatrix4fv(MVPHandle,1,false,_MVPMatrix,0);

        // get handle to fragment shader's vColor member
        int colorHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "vColor");

        float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0);

        int framePositionHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "framePosition");

        float framePosition[] = {m_frameOffset.x,m_frameOffset.y,0,0};

        GLES20.glUniform4fv(framePositionHandle,1,framePosition,0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,m_faceList.size()*3,GLES20.GL_UNSIGNED_SHORT,m_faceBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void SetFramePosition(Vector2D _pos)
    {
        m_frameOffset = _pos;
    }
}
