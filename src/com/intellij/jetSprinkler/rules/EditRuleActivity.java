package com.intellij.jetSprinkler.rules;

import android.app.Activity;
import android.os.Bundle;
import com.intellij.jetSprinkler.R;

/**
 * Created by julia on 27/06/14.
 */
public class EditRuleActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.edit_rule);
  }
}