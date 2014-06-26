package com.intellij.jetSprinkler.plantList;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class PlantListItem implements Parcelable {
  private int myNumber;
  private String myName;
  private Bitmap myBitmap;
  private Date myLastWatering;

  public PlantListItem(int number) {
    myNumber = number;
    myName = "Port " + number;
    myBitmap = null;
    myLastWatering = new Date(System.currentTimeMillis());
  }

  public void setNumber(int myNumber) {
    this.myNumber = myNumber;
  }

  public void setName(String myName) {
    this.myName = myName;
  }

  public void setBitmap(Bitmap myBitmap) {
    this.myBitmap = myBitmap;
  }

  public void setLastWatering(Date myLastWatering) {
    this.myLastWatering = myLastWatering;
  }

  public int getNumber() {
    return myNumber;
  }

  public String getName() {
    return myName;
  }

  public Bitmap getBitmap() {
    return myBitmap;
  }

  public Date getLastWatering() {
    return myLastWatering;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(myNumber);
    out.writeString(myName);
    out.writeParcelable(myBitmap, flags);
    out.writeLong(myLastWatering.getTime());
  }

  public static final Parcelable.Creator<PlantListItem> CREATOR = new Parcelable.Creator<PlantListItem>() {
    public PlantListItem createFromParcel(Parcel in) {
      PlantListItem result = new PlantListItem(in.readInt());
      result.setName(in.readString());
      result.setBitmap((Bitmap) in.readParcelable(PlantListItem.class.getClassLoader()));
      result.setLastWatering(new Date(in.readLong()));
      return result;
    }

    public PlantListItem[] newArray(int size) {
      return new PlantListItem[size];
    }
  };
}
