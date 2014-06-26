package com.intellij.jetSprinkler.plantList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.intellij.jetSprinkler.R;

import java.text.DateFormat;
import java.util.List;

public class PlantListAdapter extends ArrayAdapter<PlantListItem> {
  private Context myContext;
  private int myLayoutResourceId;
  private List<PlantListItem> myItems;

  public PlantListAdapter(Context context, int myLayoutResourceId, List<PlantListItem> items) {
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
    ImageView icon = (ImageView) row.findViewById(R.id.imgIcon);
    TextView title = (TextView) row.findViewById(R.id.txtTitle);
    TextView watering = (TextView) row.findViewById(R.id.txtWatering);

    PlantListItem plant = myItems.get(position);
    title.setText(plant.getName());
    watering.setText(DateFormat.getInstance().format(plant.getLastWatering()));
    icon.setImageBitmap(plant.getBitmap());

    return row;
  }
}
