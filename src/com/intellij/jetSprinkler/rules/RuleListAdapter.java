package com.intellij.jetSprinkler.rules;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.timetable.Rule;

import java.util.ArrayList;

public class RuleListAdapter extends ArrayAdapter<Rule> {

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
    text.setText(rule.toString());

    return row;
  }
}
