package com.bournemouthuniversity.afaiers.virtualglassesdraft;

/**
 * Created by Adam on 28/01/2018.
 */

class Photo {
    private String name;
    private String imagePath;

    public Photo(String _name, String _imagePath) {
        this.name = _name;
        this.imagePath = _imagePath;
    }

    public String GetName()
    {
        return this.name;
    }

    public String GetImagePath()
    {
        return this.imagePath;
    }

    public void SetName(String _name)
    {
        this.name = _name;
    }

    public void SetImagePath(String _imagePath)
    {
        this.imagePath = _imagePath;
    }

}
