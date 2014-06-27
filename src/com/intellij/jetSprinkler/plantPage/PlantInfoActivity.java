package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.plantList.PlantListItem;
import com.intellij.jetSprinkler.rules.EditRuleActivity;
import com.intellij.jetSprinkler.rules.RuleListAdapter;
import com.intellij.jetSprinkler.rules.SwipeDismissListViewTouchListener;

import java.text.DateFormat;
import java.util.ArrayList;

public class PlantInfoActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int REQUEST_EDIT_RULE = 2;
  public static final String PLANT_DATA = "plantData";
  private PlantListItem myData;
  private EditText myName;
  private TextView myDate;

  private final ArrayList<RuleListAdapter.Rule> rules = new ArrayList<RuleListAdapter.Rule>();
  private RuleListAdapter rulesListAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plant_info);

    myData = (PlantListItem) getIntent().getExtras().get(PLANT_DATA);

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
        editRule(new RuleListAdapter.Rule(), rules.size());
      }
    });

    updateInfo();

    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
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
      RuleListAdapter.Rule rule = (RuleListAdapter.Rule) data.getExtras().get(EditRuleActivity.RULE_DATA);
      int index = data.getIntExtra(EditRuleActivity.RULE_INDEX_DATA, -1);
      if (index >=0 && index < rules.size()) {
        rules.set(index, rule);
        rulesListAdapter.notifyDataSetChanged();
      } else if (index == rules.size()) {
        rules.add(rule);
        rulesListAdapter.notifyDataSetChanged();
      }
    }
  }

  public void editRule(RuleListAdapter.Rule rule, int position) {
    Intent intent = new Intent(PlantInfoActivity.this, EditRuleActivity.class);
    intent.putExtra(EditRuleActivity.RULE_DATA, rule);
    intent.putExtra(EditRuleActivity.RULE_INDEX_DATA, position);
    startActivityForResult(intent, REQUEST_EDIT_RULE);
  }

  private void updateInfo() {
    View view = findViewById(R.id.info);
    view.setBackground(new BitmapDrawable(myData.getBitmap()));
    myName.setText(myData.getName());
    myDate.setText("Last watering on " + DateFormat.getInstance().format(myData.getLastWatering()));
  }
}