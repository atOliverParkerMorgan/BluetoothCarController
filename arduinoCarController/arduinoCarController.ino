#include <AFMotor.h>
#include <SoftwareSerial.h>

#define PI 3.14159265

AF_DCMotor motor1(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3, MOTOR12_64KHZ);
AF_DCMotor motor4(4, MOTOR12_64KHZ);

// for analzing
int distData[360];

int con = 0;

String all = "";
const int BUFFER_SIZE = 5;
char buf[BUFFER_SIZE];
int bufferIndex = 0;
boolean foundStartOfMessage = false;

// send data
const int txPin = 0;
const int rxPin = 1;

//SoftwareSerial bt(rxPin, txPin);



boolean Stop = true;
int checkConnected = 0;
boolean isSensorOn = false;
boolean isAutomatic = false;
int data;
const int STOP = 0;
int CURRENT_STATE = STOP;
const int FORWARD_ = 1;
const int BACKWARD_ = 2;
const int RIGHT_ROTATE_BACKWARDS = 3;
const int LEFT_ROTATE_BACKWARDS = 4;
const int RIGHT_ROTATE_FORWARDS = 5;
const int LEFT_ROTATE_FORWARDS = 6;
const int SENSOR_ON = 7;
const int SENSOR_OFF = 8;
const int AUTOMATIC_ON = 9;
const int AUTOMATIC_OFF = 10;

// ultrasonic Sensor
const int echoPin = 2;
const int trigPin = 13;

int breakingDistanceInCentimetrs = 10;

const long rotateDelayConstant = 100;

int numberOfChangeStateReads = 0;
int lastState = 0;

void setup(){
    Serial.begin(9600);  //Set the baud rate to your Bluetooth module.

    pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
    pinMode(echoPin, INPUT); // Sets the echoPin as an Input
}


void loop(){
  if (Serial.available() > 0){
    con = 0;
   //Read the next available byte in the serial receive buffer
    data = Serial.read();
    if(data <= 10 and data != CURRENT_STATE){
      if(numberOfChangeStateReads == 0){
        if(data == STOP){
          setAllMotorStrength(0);
          doStop();
          return;
        }
        lastState = data;
        numberOfChangeStateReads++;

      }else if(numberOfChangeStateReads < 4 and data == lastState){
        numberOfChangeStateReads++;

      }else if(data == AUTOMATIC_ON){
        isAutomatic = true;
        rotateRightByDegrees(360);
        numberOfChangeStateReads = 0;
      }else if(data == AUTOMATIC_OFF){
        isAutomatic = false;
        numberOfChangeStateReads = 0;
      }else if(data == SENSOR_ON){
        isSensorOn = true;
        while(Serial.available() <= 0)
        breakingDistanceInCentimetrs = Serial.read();

        numberOfChangeStateReads = 0;
      }else if(data == SENSOR_OFF){
        isSensorOn = false;
        numberOfChangeStateReads = 0;
      }else{
        CURRENT_STATE = data;
        numberOfChangeStateReads = 0;
      }
      return;


  }else{
      if(con>100){
        doStop();

        return;
      }
      else{
        con++;
      }
  }


  switch (CURRENT_STATE){

    case FORWARD_:
      if(isSensorOn){
        if(calculateDistance() > breakingDistanceInCentimetrs){
          setAllMotorStrength(data);
          goForward();
        }
      }else{
        setAllMotorStrength(data);
        goForward();
      }
      break;
    case LEFT_ROTATE_BACKWARDS:
      setMotorStrengthLeft(data);
      goLeft();
      break;
    case RIGHT_ROTATE_BACKWARDS:
      setMotorStrengthRight(data);
      goRight();
      break;
    case LEFT_ROTATE_FORWARDS:
      setMotorStrengthLeft(data * 0.5);
      goForward();
      break;
    case RIGHT_ROTATE_FORWARDS:
      setMotorStrengthRight(data * 0.5);
      goForward();
      break;
    case BACKWARD_:
      setAllMotorStrength(data);
      goBack();
      break;
    }

  }
}

void setAllMotorStrength(int s){
  s = abs(s);
  motor1.setSpeed(s);
  motor2.setSpeed(s);
  motor3.setSpeed(s);
  motor4.setSpeed(s);

}

void setMotorStrengthLeft(int s){
  s = abs(s);
  motor1.setSpeed(255);
  motor2.setSpeed(s);
  motor3.setSpeed(255);
  motor4.setSpeed(s);

}

void setMotorStrengthRight(int s){
  s = abs(s);
  motor1.setSpeed(s);
  motor2.setSpeed(255);
  motor3.setSpeed(s);
  motor4.setSpeed(255);
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

void goLeft(){
  motor1.run(FORWARD);
  motor2.run(BACKWARD);
  motor3.run(FORWARD);
  motor4.run(BACKWARD);
}
void goRight(){
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
  motor3.run(BACKWARD);
  motor4.run(FORWARD);
}

void rotateRightByDegrees(int degree){
  // calculate delay
  setAllMotorStrength(250);

  int angle = 0;
  while(angle < 360){
    int command = Serial.read();
    if(command==AUTOMATIC_OFF){
        Serial.write(AUTOMATIC_OFF);
        doStop();
        break;
    }

    goRight();
    delay(10);
    doStop();

    Serial.write(byte(calculateDistance()));

    angle++;
    delay(300);
  }

  outOfLoop:
  return;
}


int calculateDistance(){

  // resets the trigPin
  digitalWrite(trigPin, LOW);
  delayMicroseconds(10);

  // Sets the trigPin on HIGH state for 10 micro seconds
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin, LOW);

  // Reads the echoPin, returns the  sound wave travel time in microseconds
  long duration = pulseIn(echoPin, HIGH);
  //Serial.println(duration*0.034/2);
  // Calculating the distance
  return duration*0.034/2;

}