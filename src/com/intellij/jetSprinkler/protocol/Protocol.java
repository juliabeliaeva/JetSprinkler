package com.intellij.jetSprinkler.protocol;

import java.util.Date;

public class Protocol {


  public static String version() {
    return CommandExecutor.executeCommand("V",null, false);
  }


  public static void setTime(Date date) {

  }

  public static Date getDate() {

  }

  public static String getSensor(int id) {

  }

  public static String getSprinklerCount(int id) {

  }

  public static Timetable getTimetable() {

  }

  public static void setTimetable(Timetable t) {

  }

}
