package com.intellij.jetSprinkler.connection.protocol;

import java.util.ArrayList;

public class Timetable {
  public ArrayList<TimetableItem> items = new ArrayList<TimetableItem>();

  public String save() {
    StringBuilder sb = new StringBuilder();
    for (TimetableItem i : items) {
      sb.append(i.save()).append(";");
    }
    return sb.toString();
  }

  public void load(String res) {
    items.clear();
    if (res.equals("")) return;
    for (String s : res.split(";")) {
      TimetableItem ti = new TimetableItem();
      ti.load(s);
      items.add(ti);
    }
  }

  public static class TimetableItem {
    public byte id;
    public int start;
    public int period;
    public int volume;

    public String save() {
      return "00000011" + ":" + id + ":" + volume + ":" + start + ":" + period;
    }

    public void load(String res) {
      String[] params = res.split(":");
      if (params.length != 5) throw new IllegalArgumentException(res);
      id = Byte.parseByte(params[1]);
      volume = Integer.parseInt(params[2]);
      start = Integer.parseInt(params[3]);
      period = Integer.parseInt(params[4]);
    }
  }
}
