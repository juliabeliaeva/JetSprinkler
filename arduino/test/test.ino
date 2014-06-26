#include "ds1302.h"

int pinLED = 13;

void setup() {
  Serial.begin(9600);
  pinMode(pinLED, OUTPUT);
  Serial.println("Test started");
  setup1302();
  setTime1302(0, 0, 23, 4, 26, 6, 2014);
  Serial.println("Press 1 to LED ON or 0 to LED OFF...");
}

long tt = 0;

void loop() {
  if (Serial.available() > 0) {  //если пришли данные
    int incomingByte = Serial.read(); // считываем байт
    test1302();
    tt = millis();
    if(incomingByte == '0') {
       digitalWrite(pinLED, LOW);  // если 1, то выключаем LED
       Serial.println("LED OFF. Press 1 to LED ON!");  // и выводим обратно сообщение
    }
    if(incomingByte == '1') {
       digitalWrite(pinLED, HIGH); // если 0, то включаем LED
       Serial.println("LED ON. Press 0 to LED OFF!");
    }
  }

  if (millis() - tt > 5000) {
    test1302();
    tt = millis();
  }
}
