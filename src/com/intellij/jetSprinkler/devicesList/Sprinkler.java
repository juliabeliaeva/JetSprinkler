package com.intellij.jetSprinkler.devicesList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.Connection;
import com.intellij.jetSprinkler.connection.protocol.Protocol;
import com.intellij.jetSprinkler.plantList.PlantsListActivity;
import com.intellij.jetSprinkler.plantPage.rules.EditRuleActivity;
import com.intellij.jetSprinkler.plantPage.rules.Rule;

import java.util.ArrayList;
import java.util.List;

public class Sprinkler extends Activity {
  private static final String STATION_NAME_PREFIX = "HC";
  private static final int REQUEST_ENABLE_BT = 1;
  public static final String STATION_NAME_DATA = "STATION_NAME";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sprinkler);

    BluetoothAdapter myBtAdapter = BluetoothAdapter.getDefaultAdapter();

    setTitle("Stations - Jet Sprinkler");

    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

    final List<StationData> devices = new ArrayList<StationData>();
    for (BluetoothDevice dev : myBtAdapter.getBondedDevices()) {
      if (!dev.getName().startsWith(STATION_NAME_PREFIX)) continue;
      devices.add(new StationData(dev.getName(), dev.getAddress()));
    }

    StationDataAdapter adapter = new StationDataAdapter(this,
            R.layout.station_list_item, devices);
    ListView list = (ListView) findViewById(R.id.listView);

    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        StationData station = devices.get(position);
        Connection.getInstance().dispose();
        if (!Connection.getInstance().init(station.getAddress())) {
          final Toast toast = Toast.makeText(Sprinkler.this.getApplicationContext(), "No connection to " + station.getName(), Toast.LENGTH_SHORT);
          toast.show();
          return;
        }

        int sprinklerCount = Protocol.getSprinklerCount();
        if (sprinklerCount == -1) return;

        Intent i = new Intent(Sprinkler.this, PlantsListActivity.class);
        i.putExtra(STATION_NAME_DATA, station.getName());
        startActivity(i);
      }
    });

    list.setAdapter(adapter);

    Button testButton = (Button) findViewById(R.id.testButton);
    testButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Rule rule = new Rule();
        Intent intent = new Intent(Sprinkler.this, EditRuleActivity.class);
        intent.putExtra(EditRuleActivity.RULE_DATA, rule);
        intent.putExtra(EditRuleActivity.RULE_INDEX_DATA, 1);
        intent.putExtra(EditRuleActivity.EDITOR_HEADER, "Water plant");
        startActivityForResult(intent, 1);
      }
    });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Connection.getInstance().dispose();
  }
}
