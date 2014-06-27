package com.intellij.jetSprinkler.protocol;

import com.intellij.jetSprinkler.Connection;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class CommandExecutor {
  public static final int COMMAND_TIMEOUT = 1000;

  //null is error
  public static String executeCommand(String s, String data, boolean expectsResult) {
    //clean buffers (bytes may be left from the previous command)
    while (true) {
      if (Connection.getInstance().read().length == 0) break;
      wait(100);
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
      //todo for "set timetable" command it can take mote than thisCommandExecutor: review
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
    if (length < 4) return null;
    for (int i = 0; i < 3; i++) {
      if (fullRes[i] != toAscii(s + "OK")[i]) return null;
    }

    if (length == 4) return expectsResult ? null : "";

    // count real checksum
    checksum = 0;
    byte asciiSharp = toAscii("#")[0];
    int index = 4; //skip cmd+"ok"+"#10"
    for (; index < length && fullRes[index] != asciiSharp; index++) {
      checksum += fullRes[index];
      checksum %= 65536;
    }

    //no checksum
    if (index == length) return null;

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
