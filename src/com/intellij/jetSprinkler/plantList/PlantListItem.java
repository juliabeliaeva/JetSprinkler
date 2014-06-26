package com.intellij.jetSprinkler.plantList;

import android.graphics.Bitmap;

import java.util.Date;

public class PlantListItem {
  private int myNumber;
  private String myName;
  private Bitmap myBitmap;
  private boolean myChecked;
  private Date myLastWatering;

  public PlantListItem(int number, String name, Bitmap bitmap, boolean checked, Date lastWatering) {
    myNumber = number;
    myName = name;
    myBitmap = bitmap;
    myChecked = checked;
    myLastWatering = lastWatering;
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

  public boolean isChecked() {
    return myChecked;
  }

  public Date getLastWatering() {
    return myLastWatering;
  }
}
