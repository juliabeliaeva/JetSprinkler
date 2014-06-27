package com.intellij.jetSprinkler.rules;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import com.intellij.jetSprinkler.R;

public class EditRuleActivity extends Activity {
  public static final String RULE_DATA = "RULE_DATA";
  public static final String RULE_INDEX_DATA = "RULE_INDEX_DATA";
  private RuleListAdapter.Rule rule;
  private int ruleIndex;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_rule);

    rule = (RuleListAdapter.Rule) getIntent().getExtras().get(RULE_DATA);
    ruleIndex = getIntent().getIntExtra(RULE_INDEX_DATA, -1);

    final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
    timePicker.setIs24HourView(true);
    timePicker.setCurrentHour(rule.getHour());
    timePicker.setCurrentMinute(rule.getMinute());

    Spinner spinner = (Spinner) findViewById(R.id.intervalPicker);
    // todo

    final NumberPicker numberPicker = (NumberPicker) findViewById(R.id.numberPicker);
    numberPicker.setValue(rule.getInterval());

    Button okButton = (Button) findViewById(R.id.saveRuleButton);
    okButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        rule.setHour(timePicker.getCurrentHour());
        rule.setMinute(timePicker.getCurrentMinute());
        rule.setInterval(numberPicker.getValue());

        Intent intent = new Intent();
        intent.putExtra(RULE_DATA, rule);
        intent.putExtra(RULE_INDEX_DATA, ruleIndex);
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }
}