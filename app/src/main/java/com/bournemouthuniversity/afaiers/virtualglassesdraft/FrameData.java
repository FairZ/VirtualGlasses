package com.bournemouthuniversity.afaiers.virtualglassesdraft;

import android.os.Parcel;
import android.os.Parcelable;

public class FrameData implements Parcelable {
    //Data Section
    private float m_eyeCentersDist;
    //TODO: ADD MESH TO DATA

    public FrameData(float _eyeCentersDist) {
        m_eyeCentersDist = _eyeCentersDist;
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //Parcelable section


    protected FrameData(Parcel in) {
        String[] data = new String[1];//TODO: Update this when adding more data

        in.readStringArray(data);
        this.m_eyeCentersDist = Float.parseFloat(data[0]);
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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
