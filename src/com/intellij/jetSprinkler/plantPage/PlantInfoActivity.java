package com.intellij.jetSprinkler.plantPage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.intellij.jetSprinkler.R;

public class PlantInfoActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.plant_info);

    Button btn = (Button) findViewById(R.id.add_button);


//    btn.setOnClickListener(new View.OnClickListener() {});
//
//    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//      startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//    }



  }
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//      Bundle extras = data.getExtras();
//      Bitmap imageBitmap = (Bitmap) extras.get("data");
//      imageView.setImageBitmap(imageBitmap);
//    }
  }
}