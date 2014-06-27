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
import com.intellij.jetSprinkler.Connection;
import com.intellij.jetSprinkler.R;
import com.intellij.jetSprinkler.plantList.PlantsListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Sprinkler extends Activity {
  private static final String STATION_NAME_PREFIX = "";
  private static final int REQUEST_ENABLE_BT = 1;

  private BluetoothAdapter myBtAdapter = null;
  private final List<StationData> myDevices = new ArrayList<StationData>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.sprinkler);

    myBtAdapter = BluetoothAdapter.getDefaultAdapter();

    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

    Set<BluetoothDevice> devices = myBtAdapter.getBondedDevices();

    for (BluetoothDevice dev : devices) {
      if (!dev.getName().startsWith(STATION_NAME_PREFIX)) continue;
      myDevices.add(new StationData(dev.getName(), dev.getAddress()));
    }

    StationDataAdapter adapter = new StationDataAdapter(this,
            R.layout.station_list_item, myDevices);
    ListView list = (ListView) findViewById(R.id.listView);

    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        StationData station = myDevices.get(position);
        if ( Connection.getInstance().init(station.getAddress())){
          Intent i = new Intent(Sprinkler.this, PlantsListActivity.class);
          i.putExtra(PlantsListActivity.DEVICE, station);
          startActivity(i);
          finish();
        }
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
