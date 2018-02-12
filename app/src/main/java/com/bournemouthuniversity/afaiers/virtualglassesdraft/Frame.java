package com.bournemouthuniversity.afaiers.virtualglassesdraft;

/**
 * Created by Adam on 28/01/2018.
 */

class Frame {
    private String name;
    private int imageRef;

    public Frame(String _name, int _imageRef) {
        this.name = _name;
        this.imageRef = _imageRef;
    }

    public String GetName()
    {
        return this.name;
    }

    public int GetImageRef()
    {
        return this.imageRef;
    }

    public void SetName(String _name)
    {
        this.name = _name;
    }

    public void SetImageRef(int _imageRef)
    {
        this.imageRef = _imageRef;
    }

}
