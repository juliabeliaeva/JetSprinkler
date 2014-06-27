package com.intellij.jetSprinkler.protocol;

import com.intellij.jetSprinkler.Connection;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Protocol {

  public static final int COMMAND_TIMEOUT = 1000;
  public static final int DATA_TIMEOUT = 2000;

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

  //null is error
  private static String executeCommand(String s, String data) {
    //clean buffers (bytes may be left from the previous command)
    while (true) {
      if (Connection.getInstance().read().length == 0) break;
      wait(50);
    }

    // ->CMD
    Connection.getInstance().write(toAscii(s));

    // <-CMD, max 1sec
    boolean timeout;
    boolean received;
    long time = System.currentTimeMillis();
    do {
      wait(50);
      byte[] read = Connection.getInstance().read();
      timeout = (System.currentTimeMillis() - time) > COMMAND_TIMEOUT;
      received = read.length == 1;
    } while (!timeout && !received);

    //command failed
    if (timeout) {
      Connection.getInstance().write(new byte[]{0xA});
      return null;
    }

    // ->'#' checksum 0xA
    int checksum = 0;
    if (data != null) {
      byte[] msg = toAscii(data);
      Connection.getInstance().write(msg);
      for (byte b : msg) {
        checksum += b;
        checksum %= 65536;
      }
    }
    Connection.getInstance().write(toAscii("#" + checksum));
    Connection.getInstance().write(new byte[]{0xA});

    // <-: read while writes
    List<byte[]> read = new LinkedList<byte[]>();
    int length = 0;
    do {
      wait(100);
      byte[] bytes = Connection.getInstance().read();
      if (bytes.length == 0) break;

      read.add(bytes);
      length += bytes.length;
    } while (true);

    byte[] fullRes = new byte[length];
    int cur = 0;
    for (byte[] arr : read) {
      System.arraycopy(arr, 0, fullRes, cur, arr.length);
      cur += arr.length;
    }

    // check that result is OK : starts with (cmd "OK" 0xA)
    for (int i = 0; i < 3; i++) {
      if (fullRes[i] != toAscii(s + "OK")[i]) return null;
    }
    if (fullRes[3] != 0x10) return null;

    // count real checksum
    checksum = 0;
    byte asciiSharp = toAscii("#")[0];
    int index = 4; //skip cmd+"ok"+"#10"
    for (; index < length && fullRes[index] != asciiSharp; index++) {
      checksum += fullRes[index];
      checksum %= 65536;
    }

    //extract target checksum
    byte[] targetCSum = new byte[fullRes.length - index - 2];
    System.arraycopy(fullRes, 0, targetCSum, 0, targetCSum.length);

    if (checksum != Integer.parseInt(fromAscii(targetCSum))) return null;

    //extract result
    byte[] result = new byte[index - 4];
    System.arraycopy(fullRes, 3, result, 0, result.length);

    return fromAscii(result);
  }

  private static byte[] toAscii(String toConvert) {
    return toConvert.getBytes(Charset.forName("US-ASCII"));
  }

  private static String fromAscii(byte[] toConvert) {
    return new String(toConvert, Charset.forName("US-ASCII"));
  }

  private static void wait(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {

    }
  }
}
