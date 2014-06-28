package com.intellij.jetSprinkler.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.protocol.Protocol;

import java.util.Date;

public class DebugActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.debug);

    final TextView tv = (TextView) findViewById(R.id.tvTime);
    Button btn = (Button) findViewById(R.id.btnTime);
    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Protocol.setTime(new Date(System.currentTimeMillis()));
        tv.setText(""+ Protocol.getDate());
      }
    });

  }
}