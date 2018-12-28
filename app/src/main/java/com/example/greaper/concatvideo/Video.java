package com.example.greaper.concatvideo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Video implements Parcelable {
    public static final Creator<Video> CREATOR = new Creator<Video>() {
        @Override
        public Video[] newArray(int size) {
            return new Video[size];
        }

        @Override
        public Video createFromParcel(Parcel source) {
            return new Video(source);
        }
    };

    public Video (Parcel source) {
        this.path = source.readString();
        this.duration = source.readInt();
        this.width = source.readInt();
        this.height = source.readInt();
    }
    private String path;
    private int duration;
    int width, height;

    public Video(String path, int duration, int width, int height) {
        this.path = path;
        this.duration = duration;
        this.width = width;
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.path);
        parcel.writeInt(this.duration);
        parcel.writeInt(this.width);
        parcel.writeInt(this.height);
    }
}
