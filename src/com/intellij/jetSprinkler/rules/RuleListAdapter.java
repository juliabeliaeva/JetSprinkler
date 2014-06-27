package com.intellij.jetSprinkler.rules;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;

import java.util.ArrayList;

public class RuleListAdapter extends ArrayAdapter<RuleListAdapter.Rule> {

  private final int resourceId;
  private final ArrayList<Rule> rules;

  public RuleListAdapter(Context context, int resourceId, ArrayList<Rule> objects) {
    super(context, resourceId, objects);
    this.resourceId = resourceId;
    this.rules = objects;
  }

  @Override
  public View getView(int position, View row, ViewGroup parent) {
    if (row == null) {
      LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
      row = inflater.inflate(resourceId, parent, false);
    }

    TextView text = (TextView) row.findViewById(R.id.ruleText);

    final Rule rule = rules.get(position);
    text.setText(rule.getHour() + ":" + rule.getMinute() + " every " + rule.getInterval() + " " + rule.getUnit().name);

    return row;
  }

  public static class Rule implements Parcelable {
    private Integer hour = 12;
    private Integer minute = 0;
    private int interval = 1;
    private UNIT unit = UNIT.DAY;

    public Integer getHour() {
      return hour;
    }

    public void setHour(Integer hour) {
      this.hour = hour;
    }

    public Integer getMinute() {
      return minute;
    }

    public void setMinute(Integer minute) {
      this.minute = minute;
    }

    public int getInterval() {
      return interval;
    }

    public void setInterval(int interval) {
      this.interval = interval;
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(hour);
      dest.writeInt(minute);
      dest.writeInt(interval);
      dest.writeInt(unit.ordinal());
    }

    public static final Parcelable.Creator<Rule> CREATOR = new Creator<Rule>() {
      @Override
      public Rule createFromParcel(Parcel source) {
        int hour = source.readInt();
        int minute = source.readInt();
        int interval = source.readInt();
        int ordinal = source.readInt();
        Rule rule = new Rule();
        rule.setHour(hour);
        rule.setMinute(minute);
        rule.setInterval(interval);
        rule.setUnit(UNIT.values()[ordinal]);
        return rule;
      }

      @Override
      public Rule[] newArray(int size) {
        return new Rule[size];
      }
    };
  }

  public static enum UNIT {
    DAY("daily"),
    WEEK("weekly"),
    MONTHS("monthly");

    private final String name;

    UNIT(String name) {
      this.name = name;
    }
  }
}
