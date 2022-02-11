#include <AFMotor.h>

#define PI 3.14159265

AF_DCMotor motor1(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3, MOTOR12_64KHZ);
AF_DCMotor motor4(4, MOTOR12_64KHZ);

char readData[1]= "0";
boolean Stop = true;
int checkConnected = 0;
char data = 'S';
boolean isSensorOn = false;
boolean isAutomtic = false;

// commands
// S - stop
// F - forward
// B - back
// L - left
// R - right

// ultrasonic Sensor
const int echoPin = 2;
const int trigPin = 3;

void setup(){
    Serial.begin(9600);  //Set the baud rate to your Bluetooth module.
    setMotorStrength(225);
    Serial.println("INITIALIZING CAR");

    pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
    pinMode(echoPin, INPUT); // Sets the echoPin as an Input
}

void loop(){
  if(Serial.available() > 0){
     checkConnected = 0;
     Serial.readBytes(readData, 6);
     data = readData[0];

     char e = readData[1];
     char a = readData[2];
     isSensorOn = e == 'E';
     Serial.println(isSensorOn);
     int firstDigit = readData[3] - '0';;
     char secondDigit = readData[4] - '0';
     char thirdDigit = readData[5] - '0';
     Serial.println(String(readData[3]));
    Serial.println(String(firstDigit));
     int strength = firstDigit * 100 + secondDigit * 10 + thirdDigit;
     Serial.println(String(strength));
     isAutomtic = a == 'A';


  }else if (checkConnected<=255){
     checkConnected++;
  }

  if(checkConnected > 255){
      data = 'S';
  }
  if(isSensorOn){
    if(caculateDistanance() <= 5){
      data = 'S';
    }
  }

  switch (data){
    case 'F':
      goForward();
      break;
    case 'L':
      goRight();
      break;
    case 'R':
      goLeft();
      break;
    case 'S':
      doStop();
      break;
    case 'B':
      goBack();
      break;
  }

}

void setMotorStrength(int s){
  motor1.setSpeed(s);
  motor2.setSpeed(s);
  motor3.setSpeed(s);
  motor4.setSpeed(s);

}
void doStop(){
  motor1.run(RELEASE);
  motor2.run(RELEASE);
  motor3.run(RELEASE);
  motor4.run(RELEASE);
}

void goBack(){
  motor1.run(BACKWARD);
  motor2.run(BACKWARD);
  motor3.run(BACKWARD);
  motor4.run(BACKWARD);
}

void goForward(){
  motor1.run(FORWARD);
  motor2.run(FORWARD);
  motor3.run(FORWARD);
  motor4.run(FORWARD);
}

void goRight(){
  motor1.run(FORWARD);
  motor2.run(BACKWARD);
  motor3.run(FORWARD);
  motor4.run(BACKWARD);
}
void goLeft(){
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
  motor3.run(BACKWARD);
  motor4.run(FORWARD);
}

int caculateDistanance(){
  // resets the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(1l);

  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // Reads the echoPin, returns the  sound wave travel time in microseconds
  long duration = pulseIn(echoPin, HIGH);

  // Calculating the distance
  return duration*0.034/2;

}