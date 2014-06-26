package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.plantList.PlantListItem;

import java.text.DateFormat;

public class PlantInfoActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  public static final String PLANT_DATA = "plantData";
  private PlantListItem myData;
  private ImageView myImg;
  private EditText myName;
  private TextView myDate;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plant_info);

    myData = (PlantListItem) getIntent().getExtras().get(PLANT_DATA);

    myImg = (ImageView) findViewById(R.id.imageView);
    myName = (EditText) findViewById(R.id.plantName);
    myDate = (TextView) findViewById(R.id.lastWatering);
    Button btn = (Button) findViewById(R.id.button);

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
      myData.setBitmap(imageBitmap);
      updateInfo();
    }
  }

  private void updateInfo() {
    myImg.setImageBitmap(myData.getBitmap());
    myName.setText(myData.getName());
    myDate.setText("Last watering on "+ DateFormat.getInstance().format(myData.getLastWatering()));
  }
}