package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Adam on 27/02/2018.
 */

public class Mesh {
    private FloatBuffer m_vertexBuffer;
    private ShortBuffer[] m_faceBuffers;
    private Shader m_shader;
    private List<String> m_vertexList;
    private List<String>[] m_faceLists;
    private Vector2D m_frameOffset;
    private List<float[]> m_colours;

    private boolean m_occludeLeft = false;
    private boolean m_occludeRight = false;

    public Mesh(Context _context, int _id)
    {
        Log.d("Mesh", "Mesh Constructor");
        //initialise variables
        int meshNum = 0;
        int maxNum = -1;
        Map<String,Integer> groups = new HashMap<>();
        m_faceBuffers = new ShortBuffer[4];
        m_vertexList = new ArrayList<>();
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
            if(line.startsWith("g "))
            {
                String[] words = line.split(" ");
                if(!groups.containsKey(words[1])) {
                    meshNum = maxNum+1;
                    maxNum+=1;
                    m_faceLists[meshNum] = new ArrayList<>();
                    groups.put(words[1],meshNum);
                }
                else {
                    meshNum = groups.get(words[1]);
                }
            }
            else if(line.startsWith("v "))
            {
                m_vertexList.add(line);
            }
            else if(line.startsWith("f "))
            {
                m_faceLists[meshNum].add(line);
            }
        }
        scanner.close();

        ByteBuffer forVertices = ByteBuffer.allocateDirect(m_vertexList.size() * 3 * 4);
        forVertices.order(ByteOrder.nativeOrder());
        m_vertexBuffer = forVertices.asFloatBuffer();

        for (String vertex : m_vertexList) {
            String xyz[] = vertex.split(" ");
            m_vertexBuffer.put(Float.parseFloat(xyz[1]));
            m_vertexBuffer.put(Float.parseFloat(xyz[2]));
            m_vertexBuffer.put(Float.parseFloat(xyz[3]));
        }
        m_vertexBuffer.position(0);

        for (int i = 0; i < 4; i++) {
            ByteBuffer forFaces = ByteBuffer.allocateDirect(m_faceLists[i].size() * 3 * 2);
            forFaces.order(ByteOrder.nativeOrder());
            m_faceBuffers[i] = forFaces.asShortBuffer();

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

        //pass the vertex positions to the vertex shader
        GLES20.glVertexAttribPointer(positionHandle, 3,
                GLES20.GL_FLOAT, false,
                3 * 4, m_vertexBuffer);

        //pass the mvp matrix to the vertex shader
        GLES20.glUniformMatrix4fv(MVPHandle, 1, false, _MVPMatrix, 0);

        float framePosition[] = {m_frameOffset.x, m_frameOffset.y, 0, 0};
        //pass frame position to the vertex shader
        GLES20.glUniform4fv(framePositionHandle, 1, framePosition, 0);

        //loop through and draw each model
        for(int i = 0; i < 4; i++) {

            //if an arm is occluded skip drawing it
            if(!(i==1&&m_occludeLeft)&&!(i==2&&m_occludeRight)) {
                //pass color to the fragment shader
                GLES20.glUniform4fv(colorHandle, 1, m_colours.get(i), 0);

                // Draw the model
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, m_faceLists[i].size() * 3, GLES20.GL_UNSIGNED_SHORT, m_faceBuffers[i]);
            }
        }
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(positionHandle);
    }

    public void SetFramePosition(Vector2D _pos)
    {
        m_frameOffset = _pos;
    }

    public void SetFrontColour(float _R, float _G, float _B, float _A )
    {
        m_colours.get(0)[0] = _R;
        m_colours.get(0)[1] = _G;
        m_colours.get(0)[2] = _B;
        m_colours.get(0)[3] = _A;
    }

    public void SetRightArmColour(float _R, float _G, float _B, float _A )
    {
        m_colours.get(1)[0] = _R;
        m_colours.get(1)[1] = _G;
        m_colours.get(1)[2] = _B;
        m_colours.get(1)[3] = _A;
    }

    public void SetLeftArmColour(float _R, float _G, float _B, float _A )
    {
        m_colours.get(2)[0] = _R;
        m_colours.get(2)[1] = _G;
        m_colours.get(2)[2] = _B;
        m_colours.get(2)[3] = _A;
    }

    public void SetLensColour(float _R, float _G, float _B, float _A )
    {
        m_colours.get(3)[0] = _R;
        m_colours.get(3)[1] = _G;
        m_colours.get(3)[2] = _B;
        m_colours.get(3)[3] = _A;
    }

    public void SetOccludes(boolean _left, boolean _right)
    {
        m_occludeLeft = _left;
        m_occludeRight = _right;
    }
}
