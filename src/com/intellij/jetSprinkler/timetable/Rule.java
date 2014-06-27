package com.intellij.jetSprinkler.timetable;

import android.os.Parcel;
import android.os.Parcelable;
import com.intellij.jetSprinkler.rules.RuleListAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
* Created by muhin on 28/06/14.
*/
public class Rule implements Parcelable {
  private Integer hour = 12;
  private int interval = 1;
  private RuleListAdapter.UNIT unit = RuleListAdapter.UNIT.DAY;

  public Integer getHour() {
    return hour;
  }

  public void setHour(Integer hour) {
    this.hour = hour;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public RuleListAdapter.UNIT getUnit() {
    return unit;
  }

  public void setUnit(RuleListAdapter.UNIT unit) {
    this.unit = unit;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public String toString() {
    String datePresentation;
    if (unit.equals(RuleListAdapter.UNIT.MINUTE)) {
      datePresentation = "";
    } else {
      Calendar date = new GregorianCalendar();
      date.set(Calendar.HOUR_OF_DAY, getHour());
      date.set(Calendar.MINUTE, 0);
      datePresentation = new SimpleDateFormat("HH:mm").format(date.getTime()) + " ";
    }
    return datePresentation + "every " + (getInterval() == 1 ? getUnit().name : getInterval() + " " + getUnit().name + "s");
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(hour);
    dest.writeInt(interval);
    dest.writeInt(unit.ordinal());
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
      rule.setUnit(RuleListAdapter.UNIT.values()[ordinal]);
      return rule;
    }

    @Override
    public Rule[] newArray(int size) {
      return new Rule[size];
    }
  };
}
