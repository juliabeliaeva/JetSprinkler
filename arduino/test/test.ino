#include "ds1302.h"

int pinLED = 13;

void setup() {
  Serial.begin(9600);
  pinMode(pinLED, OUTPUT);
  Serial.println("Test started");
  setup1302();
  setTime1302();
}

int cnt = 0;

void loop() {
  digitalWrite(pinLED, cnt % 2);
  Serial.print("Testing "); Serial.println(cnt);
  cnt++;
//  delay(500);
  loop1302();
}
