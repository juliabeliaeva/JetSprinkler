int pinLED = 13;

void setup() {
  Serial.begin(9600);
  pinMode(pinLED, OUTPUT);
  Serial.println("Test started");
}

int cnt = 0;

void loop() {
  digitalWrite(pinLED, cnt % 2);
  Serial.print("Testing "); Serial.println(cnt);
  cnt++;
  delay(500);
}
