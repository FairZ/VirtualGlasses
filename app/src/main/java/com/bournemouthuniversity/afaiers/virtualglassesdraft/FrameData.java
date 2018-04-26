package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.os.Parcel;
import android.os.Parcelable;


/*
    Parcelable class to safely send all necessary data across from catalogue to Try on activity
*/
public class FrameData implements Parcelable {
    //Data Section
    private float m_frontR;
    private float m_frontG;
    private float m_frontB;
    private float m_frontA;
    private float m_leftR;
    private float m_leftG;
    private float m_leftB;
    private float m_leftA;
    private float m_rightR;
    private float m_rightG;
    private float m_rightB;
    private float m_rightA;
    private float m_lensR;
    private float m_lensG;
    private float m_lensB;
    private float m_lensA;
    private int m_meshID;
    private String m_name;

    public FrameData(float[] _frontCol, float[] _leftCol, float[] _rightCol, float[] _lensCol, int _meshID, String _name) {
        m_frontR = _frontCol[0];
        m_frontG = _frontCol[1];
        m_frontB = _frontCol[2];
        m_frontA = _frontCol[3];
        m_leftR = _leftCol[0];
        m_leftG = _leftCol[1];
        m_leftB = _leftCol[2];
        m_leftA = _leftCol[3];
        m_rightR = _rightCol[0];
        m_rightG = _rightCol[1];
        m_rightB = _rightCol[2];
        m_rightA = _rightCol[3];
        m_lensR = _lensCol[0];
        m_lensG = _lensCol[1];
        m_lensB = _lensCol[2];
        m_lensA = _lensCol[3];
        m_meshID = _meshID;
        m_name = _name;
    }

    public float[] GetFrontCol()
    {
        float[] retVal = {m_frontR, m_frontG, m_frontB, m_frontA};
        return retVal;
    }

    public float[] GetLeftCol()
    {
        float[] retVal = {m_leftR, m_leftG, m_leftB, m_leftA};
        return retVal;
    }

    public float[] GetRightCol()
    {
        float[] retVal = {m_rightR, m_rightG, m_rightB, m_rightA};
        return retVal;
    }

    public float[] GetLensCol()
    {
        float[] retVal = {m_lensR, m_lensG, m_lensB, m_lensA};
        return retVal;
    }

    public int GetMeshID()
    {
        return m_meshID;
    }

    public String GetName()
    {
        return m_name;
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Parcelable section


    protected FrameData(Parcel in) {
        //data must be converted to and from a single data structure in a single array therefore string is used
        //order of read and write for these pieces of data must be identical
        String[] data = new String[18];

        in.readStringArray(data);
        this.m_frontR = Float.parseFloat(data[0]);
        this.m_frontG = Float.parseFloat(data[1]);
        this.m_frontB = Float.parseFloat(data[2]);
        this.m_frontA = Float.parseFloat(data[3]);
        this.m_leftR = Float.parseFloat(data[4]);
        this.m_leftG = Float.parseFloat(data[5]);
        this.m_leftB = Float.parseFloat(data[6]);
        this.m_leftA = Float.parseFloat(data[7]);
        this.m_rightR = Float.parseFloat(data[8]);
        this.m_rightG = Float.parseFloat(data[9]);
        this.m_rightB = Float.parseFloat(data[10]);
        this.m_rightA = Float.parseFloat(data[11]);
        this.m_lensR = Float.parseFloat(data[12]);
        this.m_lensG = Float.parseFloat(data[13]);
        this.m_lensB = Float.parseFloat(data[14]);
        this.m_lensA = Float.parseFloat(data[15]);
        this.m_meshID = Integer.parseInt(data[16]);
        this.m_name = data[17];
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(new String[]{
                Float.toString(this.m_frontR),
                Float.toString(this.m_frontG),
                Float.toString(this.m_frontB),
                Float.toString(this.m_frontA),
                Float.toString(this.m_leftR),
                Float.toString(this.m_leftG),
                Float.toString(this.m_leftB),
                Float.toString(this.m_leftA),
                Float.toString(this.m_rightR),
                Float.toString(this.m_rightG),
                Float.toString(this.m_rightB),
                Float.toString(this.m_rightA),
                Float.toString(this.m_lensR),
                Float.toString(this.m_lensG),
                Float.toString(this.m_lensB),
                Float.toString(this.m_lensA),
                Integer.toString(this.m_meshID),
                this.m_name
        });
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //auto-generated stubs

    public static final Creator<FrameData> CREATOR = new Creator<FrameData>() {
        @Override
        public FrameData createFromParcel(Parcel in) {
            return new FrameData(in);
        }

        @Override
        public FrameData[] newArray(int size) {
            return new FrameData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

}
