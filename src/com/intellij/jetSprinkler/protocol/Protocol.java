package com.intellij.jetSprinkler.protocol;

import com.intellij.jetSprinkler.Connection;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Protocol {


  public static String version() {
    Connection.getInstance().write(toAscii("A"));
    wait(200);
    byte[] read = Connection.getInstance().read();
    new String()
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
