package com.intellij.jetSprinkler.connection;

import com.intellij.jetSprinkler.timetable.Timetable;

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

  public static int getSprinklerCount() {
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

  /*
Формат строк в расписании.
flags:n:volume:start:period
flags:n:volume:sensor:value

flags ::= 0..255
 t000rawe - двоичное представление
   t - тип правила: 0 - по времени, 1 - по сенсору
   r - тип контроля значения: 0 - sensor<value, 1 - sensor>value
   a - включить сигнал
   w - полить
   e - 0 - disabled, 1 - enabled
n ::= номер устройства для полива (начиная с 1?)
volume ::= длительность [полива] (в секундах?)
start  ::= время первого действия (в минутах от 01.01.2014 00:00)
period ::= период действия (в минутах); должен быть больше, чем длительность
sensor ::= номер сенсора, значение которого проверяется
value  ::= значение, с которым сравнивается значение сенсора

*/
  private static String serialize(Timetable t) {
    return null;
  }

  private static Timetable deserialize(String res) {
    return null;
  }
}
