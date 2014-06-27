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
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.connection.Connection;
import com.intellij.jetSprinkler.connection.protocol.Protocol;
import com.intellij.jetSprinkler.plantList.PlantsListActivity;

import java.util.ArrayList;
import java.util.List;

public class Sprinkler extends Activity {
  private static final String STATION_NAME_PREFIX = "";
  private static final int REQUEST_ENABLE_BT = 1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sprinkler);

    BluetoothAdapter myBtAdapter = BluetoothAdapter.getDefaultAdapter();

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
        if (!Connection.getInstance().init(station.getAddress())) return;

        int sprinklerCount = Protocol.getSprinklerCount();
        if (sprinklerCount == -1) return;

        Intent i = new Intent(Sprinkler.this, PlantsListActivity.class);
        startActivity(i);
      }
    });

    list.setAdapter(adapter);

    Button showPlants = (Button) findViewById(R.id.showPlants); // todo this is temporary until we have proper navigation
    showPlants.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Sprinkler.this, PlantsListActivity.class);
        startActivity(intent);
      }
    });
  }


}
