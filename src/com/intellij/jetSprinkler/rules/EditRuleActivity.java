package com.intellij.jetSprinkler.rules;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.intellij.jetSprinkler.R;

public class EditRuleActivity extends Activity {
  public static final String RULE_DATA = "RULE_DATA";
  public static final String RULE_INDEX_DATA = "RULE_INDEX_DATA";
  public static final String EDITOR_HEADER = "EDITOR_HEADER";
  private RuleListAdapter.Rule rule;
  private int ruleIndex;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_rule);

    rule = (RuleListAdapter.Rule) getIntent().getExtras().get(RULE_DATA);
    ruleIndex = getIntent().getIntExtra(RULE_INDEX_DATA, -1);
    final String header = getIntent().getStringExtra(EDITOR_HEADER);
    setTitle(header);

    final NumberPicker timePicker = (NumberPicker) findViewById(R.id.hourPicker);
    timePicker.setMinValue(0);
    timePicker.setMaxValue(23);
    timePicker.setValue(rule.getHour());

    final NumberPicker intervalPicker = (NumberPicker) findViewById(R.id.intervalPicker);
    intervalPicker.setMinValue(1);
    intervalPicker.setMaxValue(20);
    intervalPicker.setValue(rule.getInterval());

    Spinner spinner = (Spinner) findViewById(R.id.unitPicker);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.units_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final RuleListAdapter.UNIT[] values = RuleListAdapter.UNIT.values();
        if (position >= 0 && position < values.length) {
          rule.setUnit(values[position]);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });
    spinner.setSelection(rule.getUnit().ordinal());

    Button okButton = (Button) findViewById(R.id.saveRuleButton);
    okButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        rule.setHour(timePicker.getValue());
        rule.setInterval(intervalPicker.getValue());

        Intent intent = new Intent();
        intent.putExtra(RULE_DATA, rule);
        intent.putExtra(RULE_INDEX_DATA, ruleIndex);
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }
}