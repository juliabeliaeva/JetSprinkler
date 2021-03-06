#include <EEPROM.h>
#include "ds1302.h"

const int pinLED = 13;
const int pinPump = 9;
const int pinValve[] = { 2, 3, 4, 5 };
const int pin1302CLK = 6;
const int pin1302DAT = 7;
const int pin1302RST = 8;

const int NUMBER_OF_DEVICES = sizeof(pinValve)/sizeof(int);

Stream& ttyCmd = Serial;
Stream& ttyDebug = Serial;

#define debug(x) ttyDebug.print(x)
#define debug(x)

struct Rule {
  byte flags;
  byte id;
  unsigned volume;
  long start;
  long period;
};

enum State {
  READY,
  COMMAND_STARTED,
  BUFFER_OVERFLOW
};

// input/output staff
char cmd;
const int BUFFER_SIZE = 200;
char buffer[BUFFER_SIZE+1];
char *bufferPtr;

// watering staff
const int WATERING_QUEUE_SIZE = 10;
byte wateringQueue[WATERING_QUEUE_SIZE];
byte *queueHead = wateringQueue, *queueTail = queueHead;
long lastCheck = 0;              // last time (in minutes since 2014-01-01) we checked rules for watering
int wateringID = -1;             // <0 - no watering now, otherwise - watered device
long wateringStarted = 0;        // millis() when watering for device wateringID was started
unsigned int wateringLength;     // current watering length in millis
int alarmID = -1;
long alarmStarted = 0;
unsigned int alarmLength;

// sensors
long lastwateringValues[NUMBER_OF_DEVICES] = {};
int moistureValues[NUMBER_OF_DEVICES] = {};
const int NUMBER_OF_SENSORS = NUMBER_OF_DEVICES * 2;


State state = READY;


void setup() {
  Serial.begin(9600);        // initialize hardware rx/tx
  pinMode(pinLED, OUTPUT);
  pinMode(pinPump, OUTPUT);
  for (int i=0; i<NUMBER_OF_DEVICES; ++i) pinMode(pinValve[i], OUTPUT);
  for (int i=0; i<NUMBER_OF_DEVICES; ++i) pinMode(A0+i, INPUT);
  stopWatering();
  setup1302();
  if (!testEEPROMinitialized())  initEEPROM();
  
  lastCheck = getCurrentTime();

//  ttyCmd.setTimeout(10000);  // 10 seconds for typing
}

void loop() {
  if (ttyCmd.available() > 0) {  // is there any input data
    char ch = ttyCmd.read();     // data
    switch (state) {
      case READY:                // expecting command
        if (ch == '\n' || ch == '\r')  {       // empty command
          ttyCmd.println("OK");
        } else {
          cmd = ch;
          bufferPtr = buffer;
          state = COMMAND_STARTED;
          ttyCmd.print(cmd);
        }
        break;
      case COMMAND_STARTED:      // buffer command till \n (or buffer overflow)
        if (ch == '\n' || ch == '\r') {        // end of data
          *bufferPtr = 0;
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
        if (ch == '\n' || ch == '\r') {
          ttyCmd.println("ERROR: buffer overflow");
          state = READY;
        }
        break;
    }
  }
  
  switch (state) {
    case READY: {
      // check rules if minute passed
      long curTime = getCurrentTime();
      if (curTime > lastCheck)
        checkRules(curTime);

      // reading sensor values
      for (int i=0; i<NUMBER_OF_DEVICES; ++i) {
        //debug(i); debug(" analogRead="); debug(analogRead(i)); debug(" : "); debug(analogRead(i==0 ? A0 : i==1 ? A1 : i==2 ? A2 : i==3 ? A3 : A4)); debug("\n");
        moistureValues[i] = analogRead(i);
      }

      // path through to default
    }
    default: {
      if (wateringID < 0) {      // no current watering, check watering queue
        if (sizeWatering()) {
          debug(" watering size()="); debug(sizeWatering()); debug(" ");
          // start watering
          Rule rule;
          readRule(pollWatering(), rule);
          if (rule.flags & 2) {
            wateringStarted = millis();
            wateringID = rule.id;
            wateringLength = rule.volume * 100;    // convert to milliseconds
            lastwateringValues[wateringID] = getCurrentTime();  // save "sensor" value
            startWatering(wateringID);
          }
          if (rule.flags & 4) {
            int alarmLeft = alarmID<0 ? 0 : millis() - alarmStarted;
            alarmStarted = millis();
            alarmID = rule.id;
            alarmLength = max(alarmLeft, rule.volume * 100);
            startAlarm(alarmID);
          }
        }
      } else if (millis() - wateringStarted >= wateringLength) {
        // stop watering
        stopWatering(wateringID);
        wateringID = -1;
      }
      if (alarmID >= 0 && (millis()-alarmStarted) >= alarmLength) {
        // stop alarm
        stopAlarm(alarmID);
        alarmID = -1;
      }
    }
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
    debug("(no checksum supplied)");
  }
  // process cmd
  switch (cmd) {
    case 'V':    // get version
      ttyCmd.print("OK\n");
      sendData("0.1");
      break;
    case 'N':    // get number of watering devices
      ttyCmd.print("OK\n");
      sprintf(buffer, "%d", NUMBER_OF_DEVICES);
      sendData(buffer);
      break;

    case 'S': {   // S2014-06-26 22:58:00# - set current date & time
      char *ptr = buffer;
      int year = getInt(ptr), month = getInt(++ptr), day = getInt(++ptr), hour = getInt(++ptr), minute = getInt(++ptr), second = getInt(++ptr);
      setTime1302(second, minute, hour, 1, day, month, year);
      ttyCmd.print("OK\n");
      break;
    }
    case 'G': {  // get current date & time in format 2014-06-26 22:58:00#
      ds1302_struct rtc;
      getTime1302(rtc);
      sprintf(buffer, "%04d-%02d-%02d %02d:%02d:%02d", \
        2000 + bcd2bin( rtc.Year10, rtc.Year), \
        bcd2bin( rtc.Month10, rtc.Month), \
        bcd2bin( rtc.Date10, rtc.Date), \
        bcd2bin( rtc.h24.Hour10, rtc.h24.Hour), \
        bcd2bin( rtc.Minutes10, rtc.Minutes), \
        bcd2bin( rtc.Seconds10, rtc.Seconds));
      ttyCmd.print("OK\n");
      sendData(buffer);
      break;
    }
    case 'L': {  // get timetable
      int tabsize = getRuleTableSize();
      ttyCmd.print("OK\n");
      char *ptr = buffer;
      *ptr = 0;
      struct Rule rule;
      for (int i=0; i<tabsize; ++i) {
        readRule(i, rule);
        sprintf(ptr, "%u:%u:%u:%lu:%lu;", rule.flags, rule.id, rule.volume, rule.start, rule.period);
        ptr += strlen(ptr);
      }
      sendData(buffer);
      break;
    }
    case 'T': {  // set timetable
      stopWatering();
      clearWateringQueue();
      EEPROM.write(4, 0);      // first write 0 as table length, will be fixed at the end of loading (to be reset-safe)
      char *ptr = buffer;
      int cnt = 0;
      boolean error = false;
      while (!error && isDigit(*ptr)) {
        long d[5];             // { flags, id, vol, start, period }
        for (int i=0; i<5; ++i) {
          d[i] = getLong(ptr);
          if (i<4 && *ptr==':' || i==4 && *ptr==';')  ++ptr;    // skip ';'
          else {
            if (i==4)
              ttyCmd.print("ERROR format: expected \';\'\n");
            else
              ttyCmd.print("ERROR format: expected \':\'\n");
            error = true;
            break;
          }
        }
        byte tabline[] = { d[0], d[1], d[2]>>8, d[2]&255, d[3]>>24, (d[3]>>16)&255, (d[3]>>8)&255, d[3]&255, d[4]>>24, (d[4]>>16)&255, (d[4]>>8)&255, d[4]&255 };
        for (int i=0; i<12; ++i)  EEPROM.write(5+12*cnt+i, tabline[i]);
        ++cnt;
      }
      if (error)  break;
      EEPROM.write(4, cnt);
      ttyCmd.print("OK\n");
      break;
    }

    case 'D': {    // get sensor value
      char *ptr = buffer;
      int sensorID = getInt(ptr);
      if (sensorID < 0 || sensorID >= NUMBER_OF_SENSORS) {
        ttyCmd.print("ERROR wrong sensor id\n");
        break;
      }
      ttyCmd.print("OK\n");
      if (sensorID < NUMBER_OF_DEVICES) {
        sprintf(buffer, "%ld", lastwateringValues[sensorID]);
      } else if (sensorID < 2 * NUMBER_OF_DEVICES) {
        sprintf(buffer, "%d", moistureValues[sensorID-NUMBER_OF_DEVICES]);
      }
      sendData(buffer);
      break;
    }

    case 'P':
      stopWatering();
      ttyCmd.print("OK\n");
      break;
    case 'X': {    // get pin status
      char *ptr = buffer;
      int n = getInt(ptr);
      ttyCmd.print("OK\n");
      sprintf(buffer, "Pin %d is %s", n, (digitalRead(n) ? "HIGH" : "LOW"));
      sendData(buffer);
      break;
    }
    case 'Y':     // set pin
    case 'Z': {   // reset pin
      char *ptr = buffer;
      int n = getInt(ptr);
      digitalWrite(n, cmd=='Y' ? HIGH : LOW);
      ttyCmd.print("OK\n");
      sprintf(buffer, "Pin %d set to %s", n, (cmd=='Y' ? "HIGH" : "LOW"));
      sendData(buffer);
      break;
    }
      
    default:
      sprintf(buffer, "ERROR CMD: \'%c\' (%d)\n", cmd, (int)cmd);
      ttyCmd.print(buffer);
      break;
  }
}

void stopWatering() {
  digitalWrite(pinPump, LOW);
  for (int i=0; i<NUMBER_OF_DEVICES; ++i)  digitalWrite(pinValve[i], LOW);
}

int getInt(char* &ptr) {        // advance ptr till next nondigit, return int value
  int result = 0;
  for (const char *p0=ptr; ptr<bufferPtr && isDigit(*ptr); ++ptr)
    (result *= 10) += (*ptr - '0');
  return result;
}
long getLong(char* &ptr) {
  long result = 0;
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

long getCurrentTime() {           // get time from rtc and convert to minutes since 2014-01-01 00:00
  ds1302_struct rtc;              //   return 0 or negative if error
  getTime1302(rtc);
  int year  = bcd2bin( rtc.Year10, rtc.Year) + 2000,
      month = bcd2bin( rtc.Month10, rtc.Month),
      day   = bcd2bin( rtc.Date10, rtc.Date),
      hour  = bcd2bin( rtc.h24.Hour10, rtc.h24.Hour),
      minute= bcd2bin( rtc.Minutes10, rtc.Minutes);
  const byte months[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
  long result = 0;
  result += (year - 2014) * 365 + ((year-1)%4 - 2013%4) - ((year-1)%100 - 2013%100) + ((year-1)%400 - 2013%400);
  for (int i=1; i<month; ++i)  result += months[i];                           // days in the current year
  if (month>2 && ((year%4==0) && (year%100!=0) || (year%400==0)))  result++;  // add leap day in the current year (if any)
  result += day - 1;       // at last we calculated all full days
  result *= 24;
  result += hour;          // all full hours
  result *= 60;
  result += minute;        // all full minutes
  return result;
}


//////////////////////////////////
//  EEPROM sturcture:
//
// 0000: 15 06 00 01  (1506 - signature, 00 01 - version)
// 0004: nn           ( nn - number of lines in table)
// 0005: ff id vv vv ss ss ss ss pp pp pp pp  (line ff - flags, id - device id, vvvv - length of event, ssssssss - event start, pppppppp - event period)
//
//

void initEEPROM() {
  // set initial table size to zero
  EEPROM.write(4, 0);
  // write version
  EEPROM.write(2, 0);
  EEPROM.write(3, 1);
  // write signature
  EEPROM.write(0, 0x15);
  EEPROM.write(1, 0x06);
}

boolean testEEPROMinitialized() {
  return EEPROM.read(0)==0x15 && EEPROM.read(1)==0x06 && EEPROM.read(2)==0 && EEPROM.read(3)==1 && EEPROM.read(4)<60;
}

int getRuleTableSize() {
  return EEPROM.read(4);
}

void readRule(int n, struct Rule &rule) {
  int addr = 5 + n*12;
  rule.flags  = EEPROM.read(addr);
  rule.id     = EEPROM.read(addr+1);
  rule.volume = EEPROM.read(addr+2)<<8 | EEPROM.read(addr+3);
  rule.start  = ((long)EEPROM.read(addr+4))<<24 | ((long)EEPROM.read(addr+5))<<16 | ((long)EEPROM.read(addr+ 6))<<8 | EEPROM.read(addr+7);
  rule.period = ((long)EEPROM.read(addr+8))<<24 | ((long)EEPROM.read(addr+9))<<16 | ((long)EEPROM.read(addr+10))<<8 | EEPROM.read(addr+11);
}

unsigned getVolume(int n) {
  int addr = 5 + n*12;
  return EEPROM.read(addr+2)<<8 + EEPROM.read(addr+3);
}

//////////////////////////////////////////
// Watering
//

void clearWateringQueue() {
  queueHead = queueTail = wateringQueue;
}

int sizeWatering() {
  return queueHead <= queueTail ? queueTail - queueHead : WATERING_QUEUE_SIZE - (queueHead-queueTail);
}

boolean offerWatering(byte b) {
  if (sizeWatering() >= WATERING_QUEUE_SIZE)  return false;
  *(queueTail++) = b;
  if (queueTail >= wateringQueue+WATERING_QUEUE_SIZE)  queueTail = wateringQueue;
  return true;
}

byte pollWatering() {  // return 0 if queue is empty
  if (sizeWatering() == 0)  return 0;
  byte result = *(queueHead++);
  if (queueHead >= wateringQueue+WATERING_QUEUE_SIZE)  queueHead = wateringQueue;
  return result;
}

long nextTime(unsigned long from, unsigned long start, unsigned long interval) {  // find next number >= from in start+interval*n sequence
  debug("\n nextTime("); debug(from); debug(", "); debug(start); debug(", "); debug(interval); debug(") = "); debug(from + interval - (from-start) % interval); debug("\n");
  if (start>from)  return start;
  long r = (from - start) % interval;
  return from + (r ? interval - r : 0);
}

void checkRules(long curTime) {
  int n = getRuleTableSize();
  Rule rule;
  for (int i=0; i<n; ++i) {
    readRule(i, rule);
    if (!(rule.flags & 1))  continue;  // disabled rule
    if (rule.flags & 128) {            // conditional rule
      debug(" conditional rules are not supported now :( ");
    } else {                           // timing rule
      debug(" checking rule "); debug(i); debug(", last check: "); debug(lastCheck); debug(" cur time: "); debug(curTime); debug(" nextTime: ");
      debug(nextTime(lastCheck+1, rule.start, rule.period));
      if (nextTime(lastCheck+1, rule.start, rule.period) <= curTime) {
        debug(" offer watering");
        offerWatering(i);
      }
    }
    debug("\n");
  }
  lastCheck = curTime;
  debug(sizeWatering());
}

void startWatering(int deviceID) {
  // turn on pump
  debug("\nStarting watering device="); debug(deviceID); debug("\n");
  digitalWrite(pinPump, HIGH);
  // turn on valve
  digitalWrite(pinValve[deviceID], HIGH);
}

void stopWatering(int deviceID) {
  debug("\nStopping watering device="); debug(deviceID); debug("\n");
  stopWatering();
}

void startAlarm(int deviceID) {
  debug("\nStarting alarm device="); debug(deviceID); debug("\n");
  digitalWrite(pinLED, HIGH);
}

void stopAlarm(int deviceID) {
  debug("\nStopping alarm device="); debug(deviceID); debug("\n");
  digitalWrite(pinLED, LOW);
}
