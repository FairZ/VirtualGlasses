package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.opengl.GLES20;
import android.util.Log;

/*
    Class to handle the loading, compiling, and linking of the openGL shader
*/
public class Shader {

    //program location id
    private int m_location;

    //hard coded vertex and fragment shaders are used as they are not complicated enough to warrant
    //the creation of a file parser
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "uniform mat4 MVP;" +
                    "uniform vec4 framePosition;"+
                    "void main() {" +
                    "  vec4 position = MVP * vPosition;" +
                    "  position = position / position.w;" +
                    "  position += framePosition;" +
                    "  position.w = 1.0;" +
                    "  gl_Position = position;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  vec4 colour = vColor;"+
                    "  colour.rgb *= colour.a;"+
                    "  gl_FragColor = colour;" +
                    "}";


    public Shader()
    {
        //create the program
        m_location = GLES20.glCreateProgram();

        //load each shader
        int vert = LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int frag = LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //attach the shaders to the program
        GLES20.glAttachShader(m_location, vert);
        GLES20.glAttachShader(m_location, frag);

        //link the program
        GLES20.glLinkProgram(m_location);
        //check for linkage errors
        int[] isLinked = new int[1];
        GLES20.glGetProgramiv(m_location, GLES20.GL_LINK_STATUS, isLinked, 0);
        if (isLinked[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(m_location);
            Log.d("Shader linking", error);
        }
    }

    public int GetLocation()
    {
        return m_location;
    }

    public int LoadShader(int _type, String _shader )
    {
        //create a shader
        int shaderLoc = GLES20.glCreateShader(_type);

        //pass in the source code
        GLES20.glShaderSource(shaderLoc, _shader);
        //compile the shader
        GLES20.glCompileShader(shaderLoc);
        //check for compilation errors
        int[] shaderSuccess = new int[1];
        GLES20.glGetShaderiv(shaderLoc, GLES20.GL_COMPILE_STATUS, shaderSuccess, 0);
        if(shaderSuccess[0] == GLES20.GL_FALSE)
        {
            String error = GLES20.glGetShaderInfoLog(shaderLoc);
            Log.d("Shader compiling", error);
        }

        //return the location id
        return shaderLoc;
    }
}
