package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by Adam on 27/02/2018.
 */

public class Shader {

    private int m_location;

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 MVP;" +
                    "uniform vec4 framePosition;"+
                    "void main() {" +
                    "  vec4 position = MVP * vPosition;" +
                    "  gl_Position = position + framePosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    public Shader()
    {
        m_location = GLES20.glCreateProgram();

        int vert = LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int frag = LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        GLES20.glAttachShader(m_location, vert);
        GLES20.glAttachShader(m_location, frag);

        GLES20.glLinkProgram(m_location);
    }

    public int GetLocation()
    {
        return m_location;
    }

    public int LoadShader(int _type, String _shader )
    {
        int shaderLoc = GLES20.glCreateShader(_type);

        GLES20.glShaderSource(shaderLoc, _shader);
        GLES20.glCompileShader(shaderLoc);

        int[] shaderSuccess = new int[1];
        GLES20.glGetShaderiv(shaderLoc, GLES20.GL_COMPILE_STATUS, shaderSuccess, 0);
        if(shaderSuccess[0] == GLES20.GL_FALSE)
        {
            Log.d("Shader", "shader compile failed");
            Log.d("Shader", GLES20.glGetShaderInfoLog(shaderLoc));
        }

        return shaderLoc;
    }
}
