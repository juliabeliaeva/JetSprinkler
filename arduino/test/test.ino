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
  // check control sum
  char *pHash = strchr(buffer, '#');
  if (pHash) {     // calculate and check control sum
    int calculated = checksum(buffer, pHash),
        expected   = getInt(++pHash);
    if (calculated != expected) {
      debug("checksum error! "); debug(calculated); debug("!="); debug(expected);
      ttyCmd.print("ERROR in control sum\n");
      return;
    }
  } else {         // no control sum
    // ignore for now
    debug("no control sum supplied");
  }
  // process cmd
  switch (cmd) {
    case 'V':
      ttyCmd.print("OK\n");
      sendData("0.1");
      break;
    case 'N':
      ttyCmd.print("OK\n");
      sendData("3");
      break;

    case 'S':
      setTime1302(0, 50, 23, 4, 26, 6, 2014);  //todo: parse and set time
      ttyCmd.print("OK\n");
      break;

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

int getInt(char* &ptr) {        // advance ptr till next nondigit, return int value
  int result = 0;
  for (const char *p0=ptr; ptr<bufferPtr && isDigit(*ptr); ++ptr)
    (result *= 10) += (*ptr - '0');
  return result;
}

int checksum(const char *p0, const char *p1) {
  int res = 0;
  while (p0 < p1)  res += *p0++;
  return res % 65536;
}

void sendData(const char *s) {    // adds #<checksum>\n to the end
  ttyCmd.print(s); ttyCmd.print("#"); ttyCmd.print(checksum(s, s+strlen(s))); ttyCmd.print('\n');
}
