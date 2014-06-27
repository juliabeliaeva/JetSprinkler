package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.plantList.PlantListAdapter;
import com.intellij.jetSprinkler.plantList.PlantListItem;
import com.intellij.jetSprinkler.rules.RuleListAdapter;
import com.intellij.jetSprinkler.rules.SwipeDismissListViewTouchListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class PlantInfoActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  public static final String PLANT_DATA = "plantData";
  private PlantListItem myData;
  private ImageView myImg;
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

    myImg = (ImageView) findViewById(R.id.imageView);
    myName = (EditText) findViewById(R.id.plantName);
    myDate = (TextView) findViewById(R.id.lastWatering);
    Button btn = (Button) findViewById(R.id.button);
    Button addRule = (Button) findViewById(R.id.addRule);
    addRule.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        rules.add(new RuleListAdapter.Rule());
        rulesListAdapter.notifyDataSetChanged();
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

    myImg.setOnClickListener(new View.OnClickListener() {
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
      int size = imageBitmap.getWidth();
      int y = (imageBitmap.getHeight() - size) / 2;
      Bitmap cropped = Bitmap.createBitmap(imageBitmap, 0, y, size, size);
      Bitmap resized = Bitmap.createScaledBitmap(cropped, 250, 250, true);
      myData.setBitmap(resized);
      myImg.setImageBitmap(resized);
    }
  }

  private void updateInfo() {
    myImg.setImageBitmap(myData.getBitmap());
    myName.setText(myData.getName());
    myDate.setText("Last watering on " + DateFormat.getInstance().format(myData.getLastWatering()));
  }
}