package com.bournemouthuniversity.afaiers.virtualglassesdraft;

/*
    Class to store the data, name and image resource reference for each frame
*/
class Frame {
    private String m_name;
    private int m_imageRef;
    private FrameData m_frameData;

    public Frame(String _name, int _imageRef, float[] _frontCol, float[] _leftCol, float[] _rightCol, float[] _lensCol, int _meshID) {
        this.m_name = _name;
        this.m_imageRef = _imageRef;
        this.m_frameData = new FrameData(_frontCol,_leftCol,_rightCol,_lensCol,_meshID,_name);
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
