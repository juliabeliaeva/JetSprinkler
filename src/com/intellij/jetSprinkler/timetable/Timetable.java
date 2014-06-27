package com.intellij.jetSprinkler.timetable;

import android.os.Parcel;
import android.os.Parcelable;

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
public class Timetable implements Parcelable {
  @Override
  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
  }

  public static final Parcelable.Creator<Timetable> CREATOR = new Parcelable.Creator<Timetable>() {
    public Timetable createFromParcel(Parcel in) {
      return new Timetable();
    }

    public Timetable[] newArray(int size) {
      return new Timetable[size];
    }
  };

}
