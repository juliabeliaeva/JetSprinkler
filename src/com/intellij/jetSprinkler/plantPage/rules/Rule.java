package com.intellij.jetSprinkler.plantPage.rules;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
* Created by muhin on 28/06/14.
*/
public class Rule implements Parcelable {
  private Integer hour = 12;
  private int interval = 1;
  private UNIT unit = UNIT.DAY;
  private int volume;

  public Integer getHour() {
    return hour;
  }

  public void setHour(Integer hour) {
    this.hour = hour;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public int getVolume() {
    return volume;
  }

  public UNIT getUnit() {
    return unit;
  }

  public void setUnit(UNIT unit) {
    this.unit = unit;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public String toString() {
    String datePresentation;
    if (unit.equals(UNIT.MINUTE)) {
      datePresentation = "";
    } else {
      Calendar date = new GregorianCalendar();
      date.set(Calendar.HOUR_OF_DAY, getHour());
      date.set(Calendar.MINUTE, 0);
      datePresentation = new SimpleDateFormat("HH:mm").format(date.getTime()) + " ";
    }
    return datePresentation + "every " + (getInterval() == 1 ? getUnit().name : getInterval() + " " + getUnit().name + "s")+"("+volume+"s)";
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(hour);
    dest.writeInt(interval);
    dest.writeInt(unit.ordinal());
    dest.writeInt(volume);
  }

  public static final Creator<Rule> CREATOR = new Creator<Rule>() {
    @Override
    public Rule createFromParcel(Parcel source) {
      int hour = source.readInt();
      int interval = source.readInt();
      int ordinal = source.readInt();
      Rule rule = new Rule();
      rule.setHour(hour);
      rule.setInterval(interval);
      rule.setUnit(UNIT.values()[ordinal]);
      rule.setVolume(source.readInt());
      return rule;
    }

    @Override
    public Rule[] newArray(int size) {
      return new Rule[size];
    }
  };


  public static enum UNIT {
    DAY("day", "daily", 24*60),
    WEEK("week", "weekly", 7*24*60),
    MONTHS("month", "monthly", 30*24*60),
    MINUTE("minute", "minute", 1); // todo remove after presentation

    public final String name;
    public final String adjective;
    private int minutes;

    UNIT(String name, String adjective, int minutes) {
      this.name = name;
      this.adjective = adjective;
      this.minutes = minutes;
    }

    public int howManyMinutes() {
      return minutes;
    }
  }
}
