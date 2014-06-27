package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.Protocol;
import com.intellij.jetSprinkler.plantList.PlantListItem;
import com.intellij.jetSprinkler.rules.EditRuleActivity;
import com.intellij.jetSprinkler.rules.RuleListAdapter;
import com.intellij.jetSprinkler.rules.SwipeDismissListViewTouchListener;
import com.intellij.jetSprinkler.timetable.Rule;
import com.intellij.jetSprinkler.timetable.Timetable;

import java.text.DateFormat;
import java.util.ArrayList;

public class PlantInfoActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int REQUEST_EDIT_RULE = 2;
  public static final String PLANT_DATA = "plantData";
  private PlantListItem myData;
  private Timetable myTimetable;
  private EditText myName;
  private TextView myDate;
  private TextView timeTableHeader;

  private final ArrayList<Rule> rules = new ArrayList<Rule>();
  private RuleListAdapter rulesListAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plant_info);

    myData = (PlantListItem) getIntent().getExtras().get(PLANT_DATA);
    myTimetable = Protocol.getTimetable();

    rulesListAdapter = new RuleListAdapter(this, R.layout.rule_row, rules);
    ListView list = ((ListView) findViewById(R.id.rulesList));
    list.setAdapter(rulesListAdapter);
    SwipeDismissListViewTouchListener touchListener =
            new SwipeDismissListViewTouchListener(
                    list,
                    new SwipeDismissListViewTouchListener.DismissCallbacks() {
                      @Override
                      public boolean canDismiss(int position) {
                        return true;
                      }

                      public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                          rulesListAdapter.remove(rulesListAdapter.getItem(position));
                        }
                        rulesListAdapter.notifyDataSetChanged();
                        updateInfo();
                      }
                    });
    list.setOnTouchListener(touchListener);
    list.setOnScrollListener(touchListener.makeScrollListener());
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        editRule(rules.get(position), position);
      }
    });

    myName = (EditText) findViewById(R.id.plantName);
    myDate = (TextView) findViewById(R.id.lastWatering);
    Button btn = (Button) findViewById(R.id.savePlant);
    Button addRule = (Button) findViewById(R.id.addRule);

    addRule.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        editRule(new Rule(), rules.size());
      }
    });

    myName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          myData.setName(v.getText().toString());
          updateTimetableHeader();
          return false;
        }
        return false;
      }
    });

    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!Protocol.setTimetable(myTimetable)){
          //todo show error
          return;
        }
        Intent result = new Intent();
        myData.setName(myName.getText().toString());
        result.putExtra(PlantInfoActivity.PLANT_DATA, myData);
        setResult(Activity.RESULT_OK, result);
        finish();
      }
    });

    Button takeAPicture = (Button) findViewById(R.id.takePicture);
    takeAPicture.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
      }
    });

    timeTableHeader = (TextView) findViewById(R.id.timetableHeader);

    updateInfo();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap imageBitmap = (Bitmap) extras.get("data");
      View view = findViewById(R.id.info);
      view.setBackground(new BitmapDrawable(imageBitmap));
      myData.setBitmap(imageBitmap);
    } else if (requestCode == REQUEST_EDIT_RULE && resultCode == RESULT_OK) {
      Rule rule = (Rule) data.getExtras().get(EditRuleActivity.RULE_DATA);
      int index = data.getIntExtra(EditRuleActivity.RULE_INDEX_DATA, -1);
      if (index >=0 && index < rules.size()) {
        rules.set(index, rule);
        updateTimetableHeader();
        rulesListAdapter.notifyDataSetChanged();
      } else if (index == rules.size()) {
        rules.add(rule);
        updateTimetableHeader();
        rulesListAdapter.notifyDataSetChanged();
      }
    }
  }

  public void editRule(Rule rule, int position) {
    Intent intent = new Intent(PlantInfoActivity.this, EditRuleActivity.class);
    intent.putExtra(EditRuleActivity.RULE_DATA, rule);
    intent.putExtra(EditRuleActivity.RULE_INDEX_DATA, position);
    intent.putExtra(EditRuleActivity.EDITOR_HEADER, "Water " + myData.getName());
    startActivityForResult(intent, REQUEST_EDIT_RULE);
  }

  private void updateTimetableHeader() {
    if (rules.isEmpty()) {
      timeTableHeader.setText("Not watering " + myData.getName());
    } else {
      timeTableHeader.setText("Watering " + myData.getName());
    }
  }

  private void updateInfo() {
    View view = findViewById(R.id.info);
    view.setBackground(new BitmapDrawable(myData.getBitmap()));
    myName.setText(myData.getName());
    myDate.setText("Last watering on " + DateFormat.getInstance().format(myData.getLastWatering()));
    updateTimetableHeader();
  }
}