package com.intellij.jetSprinkler.plantList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.protocol.Protocol;
import com.intellij.jetSprinkler.debug.DebugActivity;
import com.intellij.jetSprinkler.devicesList.Sprinkler;
import com.intellij.jetSprinkler.plantPage.PlantInfoActivity;

import java.util.ArrayList;

public class PlantsListActivity extends Activity {
  public static final String NAME = "NAME_";
  public static final String IMAGE_URI = "IMAGE_URI_";
  public static final int MY_CHILD_ACTIVITY = 666;
  private final ArrayList<PlantListItem> myPlants = new ArrayList<PlantListItem>();

  private PlantListAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plants_list);

    final String stationName = getIntent().getStringExtra(Sprinkler.STATION_NAME_DATA);
    setTitle(stationName + " - " + getTitle());

    int sprinklerCount;
    try {
      sprinklerCount = Protocol.getSprinklerCount();
    } catch (Throwable throwable) {
      sprinklerCount = 3;
    }

    readState(sprinklerCount);

    adapter = new PlantListAdapter(this,
            R.layout.plantlist_item_row, myPlants);

    Button btn = ((Button) findViewById(R.id.debug));
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(PlantsListActivity.this, DebugActivity.class);
        i.putExtra(Sprinkler.STATION_NAME_DATA, stationName);
        startActivityForResult(i, 123);
      }
    });

    ListView list = ((ListView) findViewById(R.id.plantsListView));
    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PlantListItem plant = myPlants.get(position);

        Intent i = new Intent(PlantsListActivity.this, PlantInfoActivity.class);
        i.putExtra(PlantInfoActivity.PLANT_DATA, plant);
        startActivityForResult(i, MY_CHILD_ACTIVITY);
      }
    });

    list.setAdapter(adapter);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case (MY_CHILD_ACTIVITY): {
        if (resultCode == Activity.RESULT_OK) {
          PlantListItem plant = ((PlantListItem) data.getExtras().get(PlantInfoActivity.PLANT_DATA));
          for (int i = 0; i < myPlants.size(); i++) {
            if (myPlants.get(i).getNumber() == plant.getNumber()) {
              myPlants.set(i, plant);
              break;
            }
          }
          adapter.notifyDataSetChanged();
        }
        break;
      }
    }
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

  private void readState(int sprinklerCount) {
    myPlants.clear();

    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

    for (int port = 0; port < sprinklerCount; port++) {
      String name = sharedPreferences.getString(NAME + port, null);
      PlantListItem res;
      if (name != null) {
        // we have info
        String imageUri = sharedPreferences.getString(IMAGE_URI + port, null);
        res = new PlantListItem(port);
        res.setName(name);

        String lastWater = Protocol.getSensor(port);
        if (lastWater!=null){
          res.setLastWatering(PlantInfoActivity.getRealDate(Integer.parseInt(lastWater)).getTime());
        }

        res.setImageFileUri(imageUri);
      } else {
        // we do not have info
        res = new PlantListItem(port);
        res.setName("Plant " + port);
      }

      myPlants.add(res);
    }
  }

  private void saveState() {
    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    for (PlantListItem plant: myPlants) {
      editor.putString(NAME + plant.getNumber(), plant.getName());
      editor.putString(IMAGE_URI + plant.getNumber(), plant.getImageFileUri());
    }

    editor.commit();
  }
}