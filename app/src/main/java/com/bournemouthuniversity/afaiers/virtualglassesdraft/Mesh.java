package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.opengl.GLES20;

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
    private FloatBuffer[] m_vertexBuffers;
    private ShortBuffer[] m_faceBuffers;
    private Shader m_shader;
    private List<String>[] m_vertexLists;
    private List<String>[] m_faceLists;
    private Vector2D m_frameOffset;
    private List<float[]> m_colours;

    public Mesh(Context _context, int _id)
    {
        //initialise variables
        int meshNum = -1;
        m_vertexBuffers = new FloatBuffer[4];
        m_faceBuffers = new ShortBuffer[4];
        m_vertexLists = new List[4];
        m_faceLists = new List[4];
        m_frameOffset = new Vector2D();
        m_colours = new ArrayList<>();
        //add default colours
        for(int i = 0; i < 4; i++)
        {
            if(i != 3)
                m_colours.add(new float[] { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f });
            else
                m_colours.add(new float[] { 0, 0, 0, 0 });
        }

        Scanner scanner = new Scanner(_context.getResources().openRawResource(_id));
        while(scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if(line.startsWith("o "))
            {
                meshNum +=1;
                m_vertexLists[meshNum] = new ArrayList<>();
                m_faceLists[meshNum] = new ArrayList<>();
            }
            else if(line.startsWith("v "))
            {
                m_vertexLists[meshNum].add(line);
            }
            else if(line.startsWith("f "))
            {
                m_faceLists[meshNum].add(line);
            }
        }
        scanner.close();

        for (int i = 0; i < 4; i++) {
            ByteBuffer forVertices = ByteBuffer.allocateDirect(m_vertexLists[i].size() * 3 * 4);
            forVertices.order(ByteOrder.nativeOrder());
            m_vertexBuffers[i] = forVertices.asFloatBuffer();

            ByteBuffer forFaces = ByteBuffer.allocateDirect(m_faceLists[i].size() * 3 * 2);
            forFaces.order(ByteOrder.nativeOrder());
            m_faceBuffers[i] = forFaces.asShortBuffer();

            for (String vertex : m_vertexLists[i]) {
                String xyz[] = vertex.split(" ");
                m_vertexBuffers[i].put(Float.parseFloat(xyz[1]));
                m_vertexBuffers[i].put(Float.parseFloat(xyz[2]));
                m_vertexBuffers[i].put(Float.parseFloat(xyz[3]));
            }
            m_vertexBuffers[i].position(0);

            for (String face : m_faceLists[i]) {
                String indices[] = face.split(" ");
                String set0[] = indices[1].split("/");
                short index0 = Short.parseShort(set0[0]);
                String set1[] = indices[2].split("/");
                short index1 = Short.parseShort(set1[0]);
                String set2[] = indices[3].split("/");
                short index2 = Short.parseShort(set2[0]);
                m_faceBuffers[i].put((short) (index0 - 1));
                m_faceBuffers[i].put((short) (index1 - 1));
                m_faceBuffers[i].put((short) (index2 - 1));
            }
            m_faceBuffers[i].position(0);
        }

        m_shader = TryOnRenderer.GetShader();
    }

    public void Draw(float[] _MVPMatrix)
    {
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(m_shader.GetLocation());

        // get handle to vertex shader's vPosition member
        int positionHandle = GLES20.glGetAttribLocation(m_shader.GetLocation(), "vPosition");

        //get the handle to the vertex shader's mvp matrix member
        int MVPHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "MVP");

        // get handle to fragment shader's vColor member
        int colorHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "vColor");

        //get the handle to the vertex shader's frame position handle
        int framePositionHandle = GLES20.glGetUniformLocation(m_shader.GetLocation(), "framePosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(positionHandle);

        //loop through and draw each model
        for(int i = 0; i < 4; i++) {

            //pass the vertex positions to the vertex shader
            GLES20.glVertexAttribPointer(positionHandle, 3,
                    GLES20.GL_FLOAT, false,
                    3 * 4, m_vertexBuffers[i]);

            //pass the mvp matrix to the vertex shader
            GLES20.glUniformMatrix4fv(MVPHandle, 1, false, _MVPMatrix, 0);

            //pass color to the fragment shader
            GLES20.glUniform4fv(colorHandle, 1, m_colours.get(i), 0);

            float framePosition[] = {m_frameOffset.x, m_frameOffset.y, 0, 0};
            //pass frame position to the vertex shader
            GLES20.glUniform4fv(framePositionHandle, 1, framePosition, 0);

            // Draw the model
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, m_faceLists[i].size() * 3, GLES20.GL_UNSIGNED_SHORT, m_faceBuffers[i]);

        }
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void SetFramePosition(Vector2D _pos)
    {
        m_frameOffset = _pos;
    }
}
