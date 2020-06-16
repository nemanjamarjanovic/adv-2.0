//#include <OneWire.h>
//#include <DallasTemperature.h>
#include <SPI.h>        
#include <Ethernet.h>
#include <EthernetUdp.h>
#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <DHT.h>
//#include <SD.h>

#define DS3231_I2C_ADDRESS 0x68

#define SERIAL_NUMBER 1000456
#define DEFAULT_MAC { 0xDE, 0xAD, 0x10, 0x00, 0x04, 0x56 };
#define DEFAULT_IP 106
#define DEFAULT_PORT 8888

#define DUGME1_PIN 7
#define DUGME2_PIN 6
#define DUGME3_PIN 5
#define KLIMA_PIN 8
#define VENTILATOR_PIN 9
#define SD_KARTICA_PIN 10

#define DHT21_PIN 14
#define DHTTYPE DHT21

DHT dht(DHT21_PIN, DHTTYPE);

LiquidCrystal_I2C lcd(0x27, 2, 1, 0, 4, 5, 6, 7, 3, POSITIVE);

IPAddress timeServer(130, 88, 203, 12); 
const int timeZone = 1;  

byte _MAC_ADDRESS[] = DEFAULT_MAC;
IPAddress _IP_ADDRESS(192, 168, 1, DEFAULT_IP);
unsigned int _PORT = DEFAULT_PORT;   
char packetBuffer[UDP_TX_PACKET_MAX_SIZE]; 
EthernetUDP Udp;

typedef struct FAZA{
  float min_temperatura;
  float max_temperatura;
  float min_vlaznost;
  float max_vlaznost;
  int min_co2;
  int max_co2;
  boolean ventilator;
  boolean klima;
  int naziv;
};

FAZA pocetnaFaza      = {
  0,0,0,0,0,0,false,false,0};
FAZA fazaPrva         = {
  20,22,4000,6000,90,95,true,true,1};
FAZA fazaDruga        = {
  14,18,4000,6000,90,95,true,true,2};
FAZA fazaTreca        = {
  16,18,1100,1500,90,95,true,true,3};
FAZA nedozvoljenaFaza = {
  0,0,0,0,0,0,false,false,4};
FAZA trenutnaFaza;

float trenutnaTemperatura,trenutnaVlaznost;
int trenutniCO2;
boolean klimaUkljucena, ventilatorUkljucen;

void setup() {

  pinMode(DUGME1_PIN, INPUT);
  pinMode(DUGME2_PIN, INPUT);
  pinMode(DUGME3_PIN, INPUT);  
  pinMode(KLIMA_PIN, OUTPUT);
  pinMode(VENTILATOR_PIN, OUTPUT);
  //pinMode(SD_KARTICA_PIN, OUTPUT);

  lcd.begin(20,4);         
  lcd.backlight();
  Ethernet.begin(_MAC_ADDRESS, _IP_ADDRESS);
  Udp.begin(DEFAULT_PORT);
  dht.begin();
  Wire.begin();
  setDS3231time(30,55,20,4,21,05,15);
  //SD.begin(4);
  Serial.begin(9600);

  trenutnaFaza = pocetnaFaza;
}

void loop() {

  // provjeri stanje dugmadi
  checkButtons();

  // procitaj vrjednosti senzora
  readSensors();

  // preduzmi akciju paljenja uredjaja
  takeAction();

  printToDisplay();

  String reply = generateReply();
  Serial.println("VALUE " + reply);
  //logToFile(reply);

  for(int i = 0;i < 5; i += 1){
    respondToNetwork(reply);
    delay(1000);
  }
  //delay(2000);

}

void checkButtons(){

  // ako je prvo pritisnuto onda je trenutnaFaza = fazaPrva itd...
  int valDugme1 = digitalRead(DUGME1_PIN);  
  int valDugme2 = digitalRead(DUGME2_PIN);
  int valDugme3 = digitalRead(DUGME3_PIN);

  if( valDugme1 == HIGH && valDugme2 == LOW && valDugme3 == LOW ){
    trenutnaFaza = fazaPrva;
  }
  else if( valDugme1 == LOW && valDugme2 == HIGH && valDugme3 == LOW ){
    trenutnaFaza = fazaDruga;
  }
  else if( valDugme1 == LOW && valDugme2 == LOW && valDugme3 == HIGH  ){
    trenutnaFaza = fazaTreca;
  }
  else if( valDugme1 == LOW && valDugme2 == LOW && valDugme3 == LOW  ){
    trenutnaFaza = pocetnaFaza;
  }
  else{
    trenutnaFaza = nedozvoljenaFaza;
  }
}


void takeAction(){
  if(trenutnaFaza.klima && (trenutnaTemperatura > trenutnaFaza.max_temperatura)){
    //postavi bit za paljenje klime
    klimaUkljucena=true;
    digitalWrite(KLIMA_PIN, HIGH);
  }
  else{
    //ugasi klimu
    klimaUkljucena=false;
    digitalWrite(KLIMA_PIN, LOW);
  }
  if(trenutnaFaza.ventilator && (trenutniCO2 > trenutnaFaza.max_co2)){
    //postavi bit za paljenje ventilatora
    ventilatorUkljucen=true;
    digitalWrite(VENTILATOR_PIN, HIGH);
  }
  else{
    //ugasi ventilator
    ventilatorUkljucen=false;
    digitalWrite(VENTILATOR_PIN, LOW);
  }
}

void readSensors(){
  trenutnaTemperatura = dht.readTemperature();
  trenutnaVlaznost = dht.readHumidity();
  trenutniCO2 = random(4000, 6000) ;
}

String printFloat(float& value){
  int intValue = value*10;
  return String(intValue/10) + String(",") + String(intValue%10);
}

String printBool(boolean& value){
  if(value){
    return "DA";
  }
  return "NE";
}

String generateReply(){
  String comma = String(";");
  String reply = String(SERIAL_NUMBER) + comma
    + printFloat(trenutnaTemperatura) + comma
    + printFloat(trenutnaVlaznost) + comma
    + String(trenutniCO2) + comma
    + printBool(klimaUkljucena) + comma
    + printBool(ventilatorUkljucen) + comma
    + String(trenutnaFaza.naziv) + comma + readTimeForNetwork();
  return reply;
}

void printToDisplay(){
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.print("Temp: " + printFloat(trenutnaTemperatura)  + "C" + " Klima:" + printBool(klimaUkljucena));
  lcd.setCursor(0,1);
  lcd.print("Vlaz: " + printFloat(trenutnaVlaznost)     + "%" + "  Vent:" + printBool(ventilatorUkljucen));
  lcd.setCursor(0,2);
  lcd.print(" CO2: " + String(trenutniCO2)                    + "    MOD: " + trenutnaFaza.naziv);
  lcd.setCursor(0,3);
  if(trenutnaFaza.naziv == 0){
    lcd.print("Izaberi mod rada!");
  }
  else if(trenutnaFaza.naziv == 4){
    lcd.print("Nedozvoljeno stanje!");
  }
  else{
    //lcd.print(printIP());
    lcd.print("OK " + readTime());
  }
}

void respondToNetwork(String reply){
  // send data over network
  // if there's data available, read a packet
  int packetSize = Udp.parsePacket();
  Serial.println("REQUEST");
  if(packetSize)
  {
    Udp.read(packetBuffer,UDP_TX_PACKET_MAX_SIZE);
    Serial.println("REQUEST " + packetBuffer[0]);
    //    if(packetBuffer[0] == 76){
    //      File myFile = SD.open("log.txt");
    //      if (myFile) {
    //        Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    //        // read from the file until there's nothing else in it:
    //        while (myFile.available()) {
    //          Udp.write( myFile.read());
    //        }
    //        // close the file:
    //        myFile.close();
    //        Udp.endPacket();
    //        SD.remove("log.txt");
    //      } 
    //    }
    //else if(packetBuffer[0] == 86){
    
    Udp.beginPacket(Udp.remoteIP(), Udp.remotePort());
    char  replyBuffer[reply.length()+1]; 
    reply.toCharArray(replyBuffer,reply.length()+1); 
    Udp.write(replyBuffer);
    Udp.endPacket();    
    Serial.println("RESPONSE " + reply);
    //}
  }
}

//void logToFile(String reply){
//  String fileName = "log.txt";
//  Serial.println("FILE");
//  char  fileNameBuffer[fileName.length() + 1]; 
//  fileName.toCharArray(fileNameBuffer, fileName.length()+1); 
//
//  File dataFile = SD.open(fileNameBuffer, FILE_WRITE);
//  if (dataFile) {
//    dataFile.println(reply);
//    Serial.println("FILE "+ reply);
//    dataFile.close();
//  }
//}  



void setDS3231time(byte second, byte minute, byte hour, byte dayOfWeek, byte
dayOfMonth, byte month, byte year)
{
  // sets time and date data to DS3231
  Wire.beginTransmission(DS3231_I2C_ADDRESS);
  Wire.write(0); // set next input to start at the seconds register
  Wire.write(decToBcd(second)); // set seconds
  Wire.write(decToBcd(minute)); // set minutes
  Wire.write(decToBcd(hour)); // set hours
  Wire.write(decToBcd(dayOfWeek)); // set day of week (1=Sunday, 7=Saturday)
  Wire.write(decToBcd(dayOfMonth)); // set date (1 to 31)
  Wire.write(decToBcd(month)); // set month
  Wire.write(decToBcd(year)); // set year (0 to 99)
  Wire.endTransmission();
}

void readDS3231time(byte *second,
byte *minute,
byte *hour,
byte *dayOfWeek,
byte *dayOfMonth,
byte *month,
byte *year)
{
  Wire.beginTransmission(DS3231_I2C_ADDRESS);
  Wire.write(0); // set DS3231 register pointer to 00h
  Wire.endTransmission();
  Wire.requestFrom(DS3231_I2C_ADDRESS, 7);
  // request seven bytes of data from DS3231 starting from register 00h
  *second = bcdToDec(Wire.read() & 0x7f);
  *minute = bcdToDec(Wire.read());
  *hour = bcdToDec(Wire.read() & 0x3f);
  *dayOfWeek = bcdToDec(Wire.read());
  *dayOfMonth = bcdToDec(Wire.read());
  *month = bcdToDec(Wire.read());
  *year = bcdToDec(Wire.read());
}


// Convert normal decimal numbers to binary coded decimal
byte decToBcd(byte val)
{
  return( (val/10*16) + (val%10) );
}
// Convert binary coded decimal to normal decimal numbers
byte bcdToDec(byte val)
{
  return( (val/16*10) + (val%16) );
}

String readTime(){
  byte second, minute, hour, dayOfWeek, dayOfMonth, month, year;
  readDS3231time(&second, &minute, &hour, &dayOfWeek, &dayOfMonth, &month, &year);

  String comma = String(";");
  String reply = String(dayOfMonth) + "." + String(month) + ".20" + String(year) + ". "+
    String(hour) + ":" + String(minute);
  return reply;
}

String readTimeForNetwork(){
  byte second, minute, hour, dayOfWeek, dayOfMonth, month, year;
  readDS3231time(&second, &minute, &hour, &dayOfWeek, &dayOfMonth, &month, &year);

  String reply = String(dayOfMonth) + "." + String(month) + "." + String(year) + "-"+
    String(hour) + ":" + String(minute) + ":" + String(second);
  return reply;
}










