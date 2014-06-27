package com.intellij.jetSprinkler.devicesList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;

import java.util.List;

public class StationDataAdapter extends ArrayAdapter<StationData>{
    private Context myContext;
    private int myLayoutResourceId;
    private List<StationData> myItems;

    public StationDataAdapter(Context context, int myLayoutResourceId, List<StationData> items) {
      super(context, myLayoutResourceId, items);
      this.myLayoutResourceId = myLayoutResourceId;
      this.myContext = context;
      this.myItems = items;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {
      if (row == null) {
        LayoutInflater inflater = ((Activity) myContext).getLayoutInflater();
        row = inflater.inflate(myLayoutResourceId, parent, false);
      }
      TextView title = (TextView) row.findViewById(R.id.txtTitle);
      TextView watering = (TextView) row.findViewById(R.id.txtWatering);

      StationData station = myItems.get(position);
      title.setText(station.getName());
      watering.setText(station.getAddress());

      return row;
    }
}
