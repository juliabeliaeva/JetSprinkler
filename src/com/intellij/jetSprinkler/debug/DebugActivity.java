package com.intellij.jetSprinkler.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.protocol.CommandExecutor;
import com.intellij.jetSprinkler.connection.protocol.Protocol;
import com.intellij.jetSprinkler.devicesList.Sprinkler;

import java.util.Date;

public class DebugActivity extends Activity {
  private TextView tv;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.debug);

    final int id = getResources().getIdentifier("android:id/titleDivider", null, null);
    View divider = findViewById(id);
    if (divider != null) {
      divider.setBackgroundColor(getResources().getColor(R.color.transparent_light_color));
    }
    setTitleColor(getResources().getColor(R.color.light_color));

    String stationName = getIntent().getStringExtra(Sprinkler.STATION_NAME_DATA);
    setTitle(stationName + " - " + getTitle());

    tv = (TextView) findViewById(R.id.tvTime);

    Button btn = (Button) findViewById(R.id.btnTimeSet);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Protocol.setTime(new Date(System.currentTimeMillis()));
      }
    });

    Button btn2 = (Button) findViewById(R.id.btnTimeGet);
    btn2.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final Date date = Protocol.getDate();
        if (date == null) return;
        tv.setText("" + date);
      }
    });

    final EditText et = (EditText) findViewById(R.id.edPort);
    Button btn3 = (Button) findViewById(R.id.btnPortGet);
    btn3.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String res = CommandExecutor.executeCommand("X", "" + et.getText().toString(), true);
        if (res==null) {
          tv.setText("fail");
          return;
        }
        tv.setText(res);
      }
    });

    Button btn4 = (Button) findViewById(R.id.btnPortOn);
    btn4.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String res = CommandExecutor.executeCommand("Y", "" + et.getText().toString(), true);
        if (res==null) {
          tv.setText("fail");
          return;
        }
        tv.setText("ok");
      }
    });

    Button btn5 = (Button) findViewById(R.id.btnPortOff);
    btn5.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String res = CommandExecutor.executeCommand("Z", "" + et.getText().toString(), true);
        if (res==null) {
          tv.setText("fail");
          return;
        }
        tv.setText("ok");
      }
    });


  }
}