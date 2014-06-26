package com.intellij.jetSprinkler.plantList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
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
  private ArrayList<PlantListItem> myPlants = new ArrayList<PlantListItem>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plantslist);

    myPlants.add(new PlantListItem(0, "Misha", null, false, new Date(10000)));
    myPlants.add(new PlantListItem(0, "Misha2", null, false, new Date(10000)));

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
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      Bitmap imageBitmap = (Bitmap) extras.get("data");
      imageView.setImageBitmap(imageBitmap);
    }
  }

}