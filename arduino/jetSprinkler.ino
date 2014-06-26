char incomingByte;  // входящие данные
int  LED = 13;      // LED подключен к 12 пину
 
void setup() {
  Serial.begin(9600); // инициализация порта
  pinMode(LED, OUTPUT);
  Serial.println("Press 1 to LED ON or 0 to LED OFF...");
}
 
void loop() {
  if (Serial.available() > 0) {  //если пришли данные
    incomingByte = Serial.read(); // считываем байт
    if(incomingByte == 0) {
       digitalWrite(LED, LOW);  // если 1, то выключаем LED
       Serial.println("LED OFF. Press 1 to LED ON!");  // и выводим обратно сообщение
    }
    if(incomingByte == 1) {
       digitalWrite(LED, HIGH); // если 0, то включаем LED
       Serial.println("LED ON. Press 0 to LED OFF!");
    }
  }
}

