package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.plantList.PlantListItem;

public class PlantInfoActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  public static final String PLANT_DATA = "plantData";
  private PlantListItem myData;
  private EditText nameField;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plant_info);

    myData = (PlantListItem) getIntent().getExtras().get(PLANT_DATA);

    final ImageView img = (ImageView) findViewById(R.id.imageView);
    img.setImageBitmap(myData.getBitmap());

    final Button btn = (Button) findViewById(R.id.button);
    nameField = (EditText) findViewById(R.id.plantName);
    nameField.setText(myData.getName());
    nameField.addTextChangedListener(new TextWatcher() {
                                       @Override
                                       public void onTextChanged(CharSequence s, int start, int before, int count) {
                                         btn.setEnabled(s.length() > 0);
                                         if (s.length() > 0) {
                                           myData.setName(s.toString());
                                         }
                                       }

                                       @Override
                                       public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                       }

                                       @Override
                                       public void afterTextChanged(Editable s) {
                                       }
                                     }
    );

    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent result = new Intent();
        result.putExtra(PlantInfoActivity.PLANT_DATA, myData);
        setResult(Activity.RESULT_OK, result);
        finish();
      }
    });

    img.setOnClickListener(new View.OnClickListener() {
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
    }
  }
}