package com.bournemouthuniversity.afaiers.virtualglassesdraft;

/*
    Class to store the name and image file path of each photo
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
