package com.intellij.jetSprinkler.plantList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

public class PlantListItem implements Parcelable {
  private int myNumber;
  private String myName;
  private Date myLastWatering;
  private String imageFileUri;

  public PlantListItem(int number) {
    myNumber = number;
    myName = "Plant " + number;
    myLastWatering = new Date(System.currentTimeMillis());
  }

  public void setName(String myName) {
    this.myName = myName;
  }

  public void setLastWatering(Date myLastWatering) {
    this.myLastWatering = myLastWatering;
  }

  public void setImageFileUri(String imageFileUri) {
    this.imageFileUri = imageFileUri;
  }

  public int getNumber() {
    return myNumber;
  }

  public String getName() {
    return myName;
  }

  public Date getLastWatering() {
    return myLastWatering;
  }

  public String getImageFileUri() {
    return imageFileUri;
  }

  public Bitmap loadSquarePreview() {
    if (imageFileUri != null) {
      try {
        Bitmap bitmap = loadBitmap(250, 250, getBuilder());
        if (bitmap != null) {
          int size = bitmap.getWidth();
          int y = (bitmap.getHeight() - size) / 2;
          Bitmap cropped = Bitmap.createBitmap(bitmap, 0, y, size, size);
          return Bitmap.createScaledBitmap(cropped, 250, 250, true);
        }
      } catch (FileNotFoundException e) {
      }
    }
    return null;
  }

  public Bitmap loadFullScreanImage(int targetW, int targetH) {
    if (imageFileUri != null) {
      try {
        return loadBitmap(targetW, targetH, getBuilder());
      } catch (FileNotFoundException e) {
      }
    }
    return null;
  }

  private InputStreamBuilder getBuilder() {
    return new InputStreamBuilder() {
      @Override
      public InputStream openStream() throws FileNotFoundException {
        return new FileInputStream(imageFileUri);
      }
    };
  }

  public static Bitmap loadBitmap(int targetW, int targetH, InputStreamBuilder builder) throws FileNotFoundException {
    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
    bmOptions.inJustDecodeBounds = true;
    BitmapFactory.decodeStream(builder.openStream(), null, bmOptions);
    int photoW = bmOptions.outWidth;
    int photoH = bmOptions.outHeight;

    // Determine how much to scale down the image
    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

    // Decode the image file into a Bitmap sized to fill the View
    bmOptions.inJustDecodeBounds = false;
    bmOptions.inSampleSize = scaleFactor;
    bmOptions.inPurgeable = true;

    return BitmapFactory.decodeStream(builder.openStream(), null, bmOptions); // this fellow is not getting it correctly, bit I do not care
  }

  public interface InputStreamBuilder {
    public InputStream openStream() throws FileNotFoundException;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeInt(myNumber);
    out.writeString(myName);
    out.writeLong(myLastWatering.getTime());
    out.writeString(imageFileUri);
  }

  public static final Parcelable.Creator<PlantListItem> CREATOR = new Parcelable.Creator<PlantListItem>() {
    public PlantListItem createFromParcel(Parcel in) {
      PlantListItem result = new PlantListItem(in.readInt());
      result.setName(in.readString());
      result.setLastWatering(new Date(in.readLong()));
      result.setImageFileUri(in.readString());
      return result;
    }

    public PlantListItem[] newArray(int size) {
      return new PlantListItem[size];
    }
  };
}
