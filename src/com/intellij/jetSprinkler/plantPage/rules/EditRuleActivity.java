package com.intellij.jetSprinkler.plantPage.rules;

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
  private Rule rule;
  private int ruleIndex;
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_rule);

    final int id = getResources().getIdentifier("android:id/titleDivider", null, null);
    View divider = findViewById(id);
    if (divider != null) {
      divider.setBackgroundColor(getResources().getColor(R.color.transparent_light_color));
    }
    setTitleColor(getResources().getColor(R.color.light_color));

    rule = (Rule) getIntent().getExtras().get(RULE_DATA);
    ruleIndex = getIntent().getIntExtra(RULE_INDEX_DATA, -1);
    final String header = getIntent().getStringExtra(EDITOR_HEADER);
    setTitle(header);

    final NumberPicker timePicker = (NumberPicker) findViewById(R.id.hourPicker);
    timePicker.setMinValue(0);
    timePicker.setMaxValue(23);
    timePicker.setValue(rule.getHour());

    final TextView everyText = (TextView) findViewById(R.id.everyText);
    final SeekBar intervalSeeker = (SeekBar) findViewById(R.id.intervalSeeker);
    intervalSeeker.setMax(20);
    intervalSeeker.setProgress(rule.getInterval());
    updateEveryText(rule.getInterval(), everyText);
    intervalSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateEveryText(progress, everyText);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    final TextView duringText = (TextView) findViewById(R.id.duringText);
    final SeekBar durationSeeker = (SeekBar) findViewById(R.id.durationSeeker);
    durationSeeker.setMax(20);
    durationSeeker.setProgress(rule.getVolume()/10);
    updateDuringText(rule.getVolume()/10, duringText);
    durationSeeker.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateDuringText(progress, duringText);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    Spinner spinner = (Spinner) findViewById(R.id.unitPicker);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.units_array, R.layout.spinner);
    adapter.setDropDownViewResource(R.layout.spinner_checked);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        final Rule.UNIT[] values = Rule.UNIT.values();
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
        final int progress = intervalSeeker.getProgress();
        rule.setInterval(progress == 0 ? 1 : progress);
        int durationProgress = durationSeeker.getProgress();
        rule.setVolume(progress == 0 ? 1 : durationProgress*10);

        Intent intent = new Intent();
        intent.putExtra(RULE_DATA, rule);
        intent.putExtra(RULE_INDEX_DATA, ruleIndex);
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }

  private void updateDuringText(int progress, TextView duringText) {
    duringText.setText("During " + (progress == 0 || progress == 1 ? "1 second" : progress + " seconds"));
  }

  private void updateEveryText(int progress, TextView everyText) {
    everyText.setText("Every " + (progress == 0 || progress == 1 ? "" : "" + progress));
  }
}