package com.intellij.jetSprinkler.plantList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.intellij.jetSprinkler.R;

import java.util.ArrayList;
import java.util.Date;

public class PlantsListActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  public static final String NAME = "NAME_";
  public static final String SIZE = "SIZE";
  private final ArrayList<PlantListItem> myPlants = new ArrayList<PlantListItem>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plants_list);

    readState();

    PlantListAdapter adapter = new PlantListAdapter(this,
            R.layout.plantlist_item_row, myPlants);

    ListView list = ((ListView) findViewById(R.id.listView));
    Button btn = (Button) findViewById(R.id.add_button);

    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(PlantsListActivity.this);

        alert.setTitle("New Plant Name");

        // Set an EditText view to get user input
        final EditText input = new EditText(PlantsListActivity.this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            String value = input.getText().toString();
          }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {

          }
        });

        alert.show();
      }
    });

    list.setAdapter(adapter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  @Override
  protected void onStop() {
    super.onStop();
    saveState();
  }

  @Override
  protected void onResume() {
    super.onResume();
    saveState();
  }

  private void readState() {
    myPlants.clear();

    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
    int plantsNumber = sharedPreferences.getInt(SIZE, 0);

    for (int i = 0; i < plantsNumber; i++) {
      String name = sharedPreferences.getString(NAME + i, "" + i);
      myPlants.add(new PlantListItem(i, name, null, false, new Date(10000)));
    }

    if (myPlants.size() == 0) {
      // for testing purposes
      myPlants.add(new PlantListItem(0, "Misha", null, false, new Date(10000)));
      myPlants.add(new PlantListItem(0, "Misha2", null, false, new Date(10000)));
    }
  }

  private void saveState() {
    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putInt(SIZE, myPlants.size());
    for (PlantListItem plant: myPlants) {
      editor.putString(NAME + plant.getNumber(), plant.getName()); // todo path to image
    }

    editor.commit();
  }
}