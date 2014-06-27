package com.intellij.jetSprinkler.plantList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.intellij.jetSprinkler.connection.Connection;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.plantPage.PlantInfoActivity;
import com.intellij.jetSprinkler.connection.protocol.Protocol;

import java.util.ArrayList;

public class PlantsListActivity extends Activity {
  public static final String NAME = "NAME_";
  public static final String PORT = "PORT_";
  public static final String IMAGE_URI = "IMAGE_URI_";
  public static final String SIZE = "SIZE";
  public static final int MY_CHILD_ACTIVITY = 666;
  private final ArrayList<PlantListItem> myPlants = new ArrayList<PlantListItem>();

  private PlantListAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plants_list);

    int sprinklerCount;
    try {
      sprinklerCount = Protocol.getSprinklerCount();
    } catch (Throwable throwable) {
      sprinklerCount = 10;
    }
    if (sprinklerCount==-1) {
      throw new RuntimeException("hallo");
    }
    readState(sprinklerCount);

    adapter = new PlantListAdapter(this,
            R.layout.plantlist_item_row, myPlants);

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
    Connection.getInstance().dispose();
  }

  private void readState(int sprinklerCount) {
    myPlants.clear();

    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
    int plantsNumber = sharedPreferences.getInt(SIZE, 0);

    for (int i = 0; i < plantsNumber; i++) {
      String name = sharedPreferences.getString(NAME + i, "Plant " + i);
      String imageUri = sharedPreferences.getString(IMAGE_URI + i, null);
      int port = sharedPreferences.getInt(PORT + i, i);
      PlantListItem res = new PlantListItem(port);
      res.setName(name);
      res.setImageFileUri(imageUri);
      myPlants.add(res);
    }
  }

  private void saveState() {
    SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    editor.putInt(SIZE, myPlants.size());
    for (int i = 0; i < myPlants.size(); i++) {
      PlantListItem plant = myPlants.get(i);
      editor.putString(NAME + i, plant.getName());
      editor.putString(IMAGE_URI + i, plant.getImageFileUri());
      editor.putInt(PORT + i, plant.getNumber());
    }

    editor.commit();
  }
}