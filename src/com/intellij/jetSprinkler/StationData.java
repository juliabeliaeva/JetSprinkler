package com.intellij.jetSprinkler;

import android.os.Parcel;
import android.os.Parcelable;

public class StationData  implements Parcelable {
  private final String name;
  private final String address;

  public StationData(String name, String address) {
    this.name = name;
    this.address = address;
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(name);
    out.writeString(address);
  }

  public static final Parcelable.Creator<StationData> CREATOR = new Parcelable.Creator<StationData>() {
    public StationData createFromParcel(Parcel in) {
      return new StationData(in.readString(),in.readString());
    }

    public StationData[] newArray(int size) {
      return new StationData[size];
    }
  };
}
