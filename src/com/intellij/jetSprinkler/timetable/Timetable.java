package com.intellij.jetSprinkler.timetable;

import android.os.Parcel;
import android.os.Parcelable;

public class Timetable implements Parcelable {
  @Override
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
  }

  public static final Parcelable.Creator<Timetable> CREATOR = new Parcelable.Creator<Timetable>() {
    public Timetable createFromParcel(Parcel in) {
      return new Timetable();
    }

    public Timetable[] newArray(int size) {
      return new Timetable[size];
    }
  };

}
