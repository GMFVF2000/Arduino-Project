#include <SimpleDHT.h>
int pinDHT11 = 2;
SimpleDHT11 dht11(pinDHT11);

void setup() {
  Serial.begin(115200);
  pinMode(2, INPUT);
}

String JSONResponse(bool success, int temperature, int humidity, String error) {
  String s = "{";
  s += "\"status\" : " + String(success ? "true" : "false");
  s += ",\"temperature\" : " + String(temperature);
  s += ",\"humidity\" : " + String(humidity);
  s += ",\"error\" : " + String(error.length() > 0 ? ("\"" + error + "\"") : "NULL") ;
  s += "}";
  return s;
}

void loop() {
  byte temperature = 0;
  byte humidity = 0;
  int err = SimpleDHTErrSuccess;
  String response = ((err = dht11.read(&temperature, &humidity, NULL)) != SimpleDHTErrSuccess) 
    ? JSONResponse(false, 0, 0, "Read DHT11 failed, err=" + String(err))
    : JSONResponse(true, (int)temperature, (int)humidity, "");
  Serial.println(response);
  delay(1000);
}
