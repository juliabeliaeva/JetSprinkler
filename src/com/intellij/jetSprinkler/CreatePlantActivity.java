package com.intellij.jetSprinkler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class CreatePlantActivity extends Activity {
  private static final int REQUEST_IMAGE_CAPTURE = 1;
  private Button takeAPicButton;
  private Button okButton;
  private Button cancelButton;
  private EditText plantName;
  private ImageView imageView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.create_plant);

    takeAPicButton = (Button) findViewById(R.id.capturePlantImage);
    okButton = (Button) findViewById(R.id.okButton);
    cancelButton = (Button) findViewById(R.id.cancelButton);
    plantName = (EditText) findViewById(R.id.plantName);
    imageView = (ImageView) findViewById(R.id.imageView);

    takeAPicButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
      }
    });

    okButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
    okButton.setEnabled(false);

    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

    plantName.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        //Check if 's' is empty
        okButton.setEnabled(s.length() > 0);
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
                                    int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });
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