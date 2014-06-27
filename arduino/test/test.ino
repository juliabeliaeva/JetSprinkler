#include "ds1302.h"

const int pinLED = 13;
const int pinPump = 9;
int pinValve[] = { 2, 3, 4, 5 };
const int pin1302CLK = 6;
const int pin1302DAT = 7;
const int pin1302RST = 8;

Stream& ttyCmd = Serial;
Stream& ttyDebug = Serial;

#define debug(x) ttyDebug.print(x)

void setup() {
  Serial.begin(9600);        // initialize hardware rx/tx
  pinMode(pinLED, OUTPUT);
  pinMode(pinPump, OUTPUT);
  for (int i=0; i<(sizeof(pinValve)/sizeof(int)); ++i) pinMode(pinValve[i], OUTPUT);
  stopWatering();
  ttyCmd.println("Test started");
  setup1302();
//  ttyCmd.setTimeout(10000);  // 10 seconds for typing
}

enum State {
  READY,
  COMMAND_STARTED,
  BUFFER_OVERFLOW
};
char cmd;
const int BUFFER_SIZE = 200;
char buffer[BUFFER_SIZE+1];
char *bufferPtr;

State state = READY;

long tt = 0;

void loop() {
  if (ttyCmd.available() > 0) {  // is there any input data
    char ch = ttyCmd.read();     // data
    switch (state) {
      case READY:                // expecting command
        if (ch == '\n')  {       // empty command
          ttyCmd.println("OK");
        } else {
          cmd = ch;
          bufferPtr = buffer;
          state = COMMAND_STARTED;
          ttyCmd.print(cmd);
        }
        break;
      case COMMAND_STARTED:      // buffer command till \n (or buffer overflow)
        if (ch == '\n') {        // end of data
          // process command
          processCmd();
          state = READY;
        } else if (bufferPtr >= buffer+BUFFER_SIZE) {
          state = BUFFER_OVERFLOW;
        } else {
          *(bufferPtr++) = ch;
        }
        break;
      case BUFFER_OVERFLOW:
        if (ch == '\n') {
          ttyCmd.println("ERROR: buffer overflow");
          state = READY;
        }
        break;
    }
    // just for test
    tt = millis();
  }

  // just test output for now...
  if (millis() - tt > 5000) {
    test1302();
    tt = millis();
  }
}

void processCmd() {
  switch (cmd) {
    case 'V':
      ttyCmd.print("OK\n0.1\n");
      break;
    case 'N':
      ttyCmd.print("OK\n3\n");
      break;

    case 'S':
      setTime1302(0, 50, 23, 4, 26, 6, 2014);  //todo: parse and set time
      ttyCmd.print("OK\n");

    case 'P':
      stopWatering();
      ttyCmd.print("OK\n");
      break;
    case 'Y':
    case 'Z':
    {
      char *ptr = buffer;
      int n = getInt(ptr);
      digitalWrite(n, cmd=='Y' ? HIGH : LOW);
      ttyCmd.print("OK\nSet pin "); ttyCmd.print(n); ttyCmd.print(" to "); ttyCmd.println(cmd=='Y' ? "HIGH" : "LOW");
      break;
    }
      
    default:
      ttyCmd.print("ERROR CMD: '"); ttyCmd.print(cmd); ttyCmd.println("'");
      break;
  }
}

void stopWatering() {
  digitalWrite(pinPump, LOW);
  for (int i=0; i<(sizeof(pinValve)/sizeof(int)); ++i)  digitalWrite(pinValve[i], LOW);
}

int getInt(char* &ptr) {  // advance ptr till next nondigit, return int value
    char *p0 = ptr;
    while (ptr<bufferPtr && isDigit(*ptr)) ++ptr;
    char ch = *ptr;
    *ptr = 0;
    int n = atoi(buffer);
    *ptr = ch;
    return n;
}
