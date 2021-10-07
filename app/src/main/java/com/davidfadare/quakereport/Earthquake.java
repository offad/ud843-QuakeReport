package com.davidfadare.quakereport;
/**
 * Created by FEMI on 01/04/2018.
 */
public class Earthquake {
    private Double mMag;
    private String mLocation;
    private long mDate;
    private String url;

    public Earthquake(Double mag, String location, long date, String address){
        mMag = mag;
        mLocation = location;
        mDate = date;
        url = address;
    }

    public Double getMag() {
        return mMag;
    }

    public String getLocation() {
        return mLocation;
    }

    public long getDate() {
        return mDate;
    }

    public String getUrl() {
        return url;
    }
}
