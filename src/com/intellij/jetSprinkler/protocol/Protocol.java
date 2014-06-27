package com.intellij.jetSprinkler.protocol;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Protocol {
  public static final String DATE_FORMAT = "yyyy-MM-dd' 'HH:mm:ss";

  public static String version() {
    return CommandExecutor.executeCommand("V", null, true);
  }

  public static boolean setTime(Date date) {
    DateFormat df = new SimpleDateFormat(DATE_FORMAT);
    return CommandExecutor.executeCommand("S", df.format(date), false) != null;
  }

  public static Date getDate() {
    String time = CommandExecutor.executeCommand("G", null, true);
    if (time == null) return null;
    try {
      return new SimpleDateFormat(DATE_FORMAT).parse(time);
    } catch (ParseException e) {
      return null;
    }
  }

  public static String getSensor(int id) {
    return CommandExecutor.executeCommand("D", id + "", true);
  }

  public static int getSprinklerCount(int id) {
    String res = CommandExecutor.executeCommand("N", null, true);
    if (res == null) return -1;
    return Integer.parseInt(res);
  }

  public static Timetable getTimetable() {
    String res = CommandExecutor.executeCommand("L", null, true);
    if (res == null) return null;
    return deserialize(res);
  }

  public static boolean setTimetable(Timetable t) {
    return CommandExecutor.executeCommand("T", serialize(t), true) != null;
  }

  private static String serialize(Timetable t) {
    return null;
  }

  private static Timetable deserialize(String res) {
    return null;
  }
}
