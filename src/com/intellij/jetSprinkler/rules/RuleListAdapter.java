package com.intellij.jetSprinkler.rules;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

//    TimePicker picker = (TimePicker) row.findViewById(R.id.timePicker);
//    Spinner spinner = (Spinner) row.findViewById(R.id.intervalPicker);
//    picker.setIs24HourView(true);
//    picker.setCurrentHour(rule.getHour());
//    picker.setCurrentHour(rule.getMinute());

    return row;
  }

  public static class Rule {
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
  }

  public static enum UNIT {
    DAY("days"),
    WEEK("weeks"),
    MONTHS("months");

    private final String name;

    UNIT(String name) {
      this.name = name;
    }
  }
}
