package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.content.Context;

/**
 * Created by Adam on 28/01/2018.
 */

class Frame {
    private String m_name;
    private int m_imageRef;
    private FrameData m_frameData;

    public Frame(String _name, int _imageRef, float _eyeCentersDist) {
        this.m_name = _name;
        this.m_imageRef = _imageRef;
        this.m_frameData = new FrameData(_eyeCentersDist);
    }

    public String GetName()
    {
        return this.m_name;
    }

    public int GetImageRef()
    {
        return this.m_imageRef;
    }

    public FrameData GetFrameData() {
        return  this.m_frameData;
    }

    public void SetName(String _name)
    {
        this.m_name = _name;
    }

    public void SetImageRef(int _imageRef)
    {
        this.m_imageRef = _imageRef;
    }

}
