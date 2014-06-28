package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.protocol.Protocol;
import com.intellij.jetSprinkler.connection.protocol.Timetable;
import com.intellij.jetSprinkler.plantList.PlantListItem;
import com.intellij.jetSprinkler.plantPage.rules.EditRuleActivity;
import com.intellij.jetSprinkler.plantPage.rules.Rule;
import com.intellij.jetSprinkler.plantPage.rules.RuleListAdapter;
import com.intellij.jetSprinkler.plantPage.rules.SwipeDismissListViewTouchListener;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PlantInfoActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private static final int REQUEST_EDIT_RULE = 2;
  public static final String PLANT_DATA = "plantData";
  private PlantListItem myData;
  private EditText myName;
  private TextView myDate;
  private TextView timeTableHeader;
  private Timetable loaded;

  private File myFile;

  private final ArrayList<Rule> rules = new ArrayList<Rule>();
  private RuleListAdapter rulesListAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plant_info);

    myData = (PlantListItem) getIntent().getExtras().get(PLANT_DATA);
    Timetable timetable = null;
    try {
      timetable = Protocol.getTimetable();
    } catch (Throwable t) {
      Log.e("", "error", t);
      Toast.makeText(getApplicationContext(), "Unable to load timetable", Toast.LENGTH_SHORT).show();
    }
    if(timetable != null) {
      rulesFromTimetable(timetable);
    }

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
        try {
          if (!Protocol.setTimetable(timetableFromRules())) {
            //todo show error
            return;
          }
        } catch (Throwable t) {
          Log.e("qwe", "qwe", t);
          Toast.makeText(getApplicationContext(), "Unable to save timetable", Toast.LENGTH_SHORT).show();
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

        try {
          myFile = createImageFile();
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(myFile));
        } catch (IOException e) {
          //fuck it
        }
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
      }
    });

    timeTableHeader = (TextView) findViewById(R.id.timetableHeader);

    updateBackground();
    updateInfo();
  }

  private Timetable timetableFromRules() {
    Timetable tt = new Timetable();
    for (Timetable.TimetableItem i : loaded.items) {
      if (i.id != myData.getNumber()) {
        tt.items.add(i);
      }
    }
    for (Rule r : rules) {
      Timetable.TimetableItem ti = new Timetable.TimetableItem();
      ti.id = (byte) myData.getNumber();
      ti.period = r.getInterval() * r.getUnit().howManyMinutes();

      Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00"));
      cal.setTime(new Date(System.currentTimeMillis()));
      if (cal.get(Calendar.HOUR_OF_DAY) > r.getHour()) {
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
      }
      cal.set(Calendar.HOUR_OF_DAY, r.getHour());

      ti.start = ((int) ((cal.getTimeInMillis() - getStartCalendar().getTimeInMillis()) / (1000 * 60)));
      ti.volume = r.getVolume();

      tt.items.add(ti);
    }
    return tt;
  }

  private Calendar getStartCalendar() {
    Calendar calStart = Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00"));
    calStart.set(2014, Calendar.JANUARY, 1);
    return calStart;
  }

  private void rulesFromTimetable(Timetable timetable) {
    loaded = timetable;
    for (Timetable.TimetableItem ti : timetable.items) {
      if (ti.id != myData.getNumber()) continue;
      Rule r = new Rule();

      Rule.UNIT right = null;
      for (Rule.UNIT u : Rule.UNIT.values()) {
        if (ti.period % u.howManyMinutes() == 0) {
          right = u;
        }
      }
      assert right != null;

      r.setInterval(ti.period / right.howManyMinutes());
      r.setUnit(right);

      Calendar cal = getStartCalendar();
      cal.add(Calendar.MINUTE, ti.start);

      r.setHour(cal.get(Calendar.HOUR_OF_DAY));
      r.setVolume(ti.volume);
      rules.add(r);
    }
  }

  private void updateBackground() {
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);

    Bitmap bitmap = myData.loadFullScreanImage(size.x, size.y);

    View view = findViewById(R.id.info);
    if (bitmap != null) {
      view.setBackground(new BitmapDrawable(bitmap));
    } else {
      TypedArray array = getTheme().obtainStyledAttributes(new int[]{
              android.R.attr.colorBackground,
      });
      int backgroundColor = array.getColor(0, 0xFF00FF);
      array.recycle();
      view.setBackground(new ColorDrawable(backgroundColor));
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE) {
      Display display = getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);

      myData.setImageFileUri(myFile.getAbsolutePath());

      updateBackground();
    } else if (requestCode == REQUEST_EDIT_RULE && resultCode == RESULT_OK) {
      Rule rule = (Rule) data.getExtras().get(EditRuleActivity.RULE_DATA);
      int index = data.getIntExtra(EditRuleActivity.RULE_INDEX_DATA, -1);
      if (index >= 0 && index < rules.size()) {
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
      timeTableHeader.setText("No Rules");
    } else {
      timeTableHeader.setText("Rules " + myData.getName());
    }
  }

  private void updateInfo() {
    myName.setText(myData.getName());
    myDate.setText("Last watering on " + DateFormat.getInstance().format(myData.getLastWatering()));
    updateTimetableHeader();
  }

  private File createImageFile() throws IOException {
    // Create an image file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
    );

    return image;
  }
}