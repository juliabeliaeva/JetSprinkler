#include "ds1302.h"

const int pinLED = 13;
const int pinPump = 9;
int pinValve[] = { 2, 3, 4, 5 };
const int pin1302CLK = 6;
const int pin1302DAT = 7;
const int pin1302RST = 8;


void setup() {
  Serial.begin(9600);
  pinMode(pinLED, OUTPUT);
  pinMode(pinPump, OUTPUT);
  for (int i=0; i<(sizeof(pinValve)/sizeof(int)); ++i) pinMode(pinValve[i], OUTPUT);
  stopWatering();
  Serial.println("Test started");
  setup1302();
  setTime1302(0, 0, 23, 4, 26, 6, 2014);
  Serial.println("Press 1 to LED ON or 0 to LED OFF...");
  Serial.setTimeout(10000);  // 10 seconds for typing
}

long tt = 0;

void loop() {
  if (Serial.available() > 0) {  // is there any command?
    char cmd = Serial.read();    // read command
    switch (cmd) {
      case '0':
        digitalWrite(pinLED, LOW);  // если 1, то выключаем LED
        Serial.println("0LED OFF. Press 1 to LED ON!");  // и выводим обратно сообщение
        break;
      case '1':
        digitalWrite(pinLED, HIGH); // если 0, то включаем LED
        Serial.println("1LED ON. Press 0 to LED OFF!");
        break;
      case 'N':
        Serial.print("N3\n");
        break;
      case 'V':
        Serial.print("V0.1\n");
        break;
      case 'Y':
      {
        Serial.print("Y");
        int n = Serial.parseInt(); Serial.print(n);
        digitalWrite(n, HIGH);
        Serial.read();  // read \n
        break;
      }
      case 'Z':
      {
        Serial.print("Z");
        int n = Serial.parseInt(); Serial.print(n);
        digitalWrite(n, LOW);
        Serial.read();  // read \n
        break;
      }
      case 'P':
      {
        Serial.print("P");
        stopWatering();
        break;
      }
        
      default:
        Serial.print(" ERROR CMD: '"); Serial.print(cmd); Serial.println("'");
        break;
    }
    tt = millis();
  }

  if (millis() - tt > 5000) {
    test1302();
    tt = millis();
  }
}

void stopWatering() {
  digitalWrite(pinPump, LOW);
  for (int i=0; i<(sizeof(pinValve)/sizeof(int)); ++i)  digitalWrite(pinValve[i], LOW);
}
