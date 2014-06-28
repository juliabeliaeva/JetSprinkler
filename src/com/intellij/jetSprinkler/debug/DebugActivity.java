package com.intellij.jetSprinkler.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.protocol.Protocol;
import com.intellij.jetSprinkler.devicesList.Sprinkler;

import java.util.Date;

public class DebugActivity extends Activity {
  private TextView tv;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.debug);

    String stationName = getIntent().getStringExtra(Sprinkler.STATION_NAME_DATA);
    setTitle(stationName + " - " + getTitle());

    tv = (TextView) findViewById(R.id.tvTime);
    updateDateView();

    Button btn = (Button) findViewById(R.id.btnTime);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Protocol.setTime(new Date(System.currentTimeMillis()));
        updateDateView();
      }
    });
  }

  private void updateDateView() {
    final Date date = Protocol.getDate();
    if (date != null) {
      tv.setText("" + date);
    } else {
      tv.setText("No date set");
    }
  }
}