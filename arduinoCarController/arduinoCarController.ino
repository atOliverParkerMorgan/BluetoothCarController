#include <AFMotor.h>

#define PI 3.14159265

AF_DCMotor motor1(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3, MOTOR12_64KHZ);
AF_DCMotor motor4(4, MOTOR12_64KHZ);


const int BUFFER_SIZE = 20;
char buf[BUFFER_SIZE];
int bufferIndex = 0;
boolean foundStartOfMessage = false;


boolean Stop = true;
int checkConnected = 0;
boolean isSensorOn = false;
boolean isAutomtic = false;

int motorDir1 = RELEASE;
int motorDir2 = RELEASE;
int motorDir3 = RELEASE;
int motorDir4 = RELEASE;

int strengthMotor1 = 0;
int strengthMotor2 = 0;
int strengthMotor3 = 0;
int strengthMotor4 = 0;

// ultrasonic Sensor

const int echoPin = 2;
const int trigPin = 13;

void setup(){
    Serial.begin(9600);  //Set the baud rate to your Bluetooth module.
    Serial.println("INITIALIZING CAR");

    pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
    pinMode(echoPin, INPUT); // Sets the echoPin as an Input
}

void loop(){
  while (Serial.available() > 0){

   checkConnected = 0; 

   //Read the next available byte in the serial receive buffer
   char inByte = Serial.read();

   if(inByte=='<'){foundStartOfMessage=true;}

   //Message coming in (check not terminating character) and guard for over message size
   if ( inByte != '>' && (bufferIndex < BUFFER_SIZE - 1) && foundStartOfMessage)
   {
     //Add the incoming byte to our message
     buf[bufferIndex] = inByte;
     bufferIndex++;
   }
   //Full message received...
   else if(foundStartOfMessage){
     foundStartOfMessage = false;
     buf[bufferIndex] = '/0';
     //Add null character to string

     //Print the message (or do other things)
     Serial.println(buf);
      
     //Reset for the next message
     bufferIndex = 0;

     isSensorOn = buf[1] == 'E';
     isAutomtic = buf[2] == 'A';
     
     motorDir1 = RELEASE;
     if(buf[3] == 'F'){
        motorDir1 = FORWARD;
     }else if(buf[3] == 'B'){
        motorDir1 = BACKWARD;
     }
     strengthMotor1 = getStrengthFromInput(buf[4], buf[5], buf[6]);
     
     motorDir2 = RELEASE;
     if(buf[7] == 'F'){
        motorDir2 = FORWARD;
     }else if(buf[7] == 'B'){
        motorDir2 = BACKWARD;
     }
     strengthMotor2 = getStrengthFromInput(buf[8], buf[9], buf[10]);
     
     motorDir3 = RELEASE;
     if(buf[11] == 'F'){
        motorDir3 = FORWARD;
     }else if(buf[11] == 'B'){
        motorDir3 = BACKWARD;
     }
     strengthMotor3 = getStrengthFromInput(buf[12], buf[13], buf[14]);
     
     motorDir4 = RELEASE;
     if(buf[15] == 'F'){
        motorDir4 = FORWARD;
     }else if(buf[15] == 'B'){
        motorDir4 = BACKWARD;
     }
     strengthMotor4 = getStrengthFromInput(buf[16], buf[17], buf[18]);
//     
//     for( int i = 0; i < BUFFER_SIZE; i++){
 //          Serial.print(String(buf[i]));   
 //    } 


   }
 }
     
     
  if (checkConnected<=1000){
     checkConnected++;
  }
  
  if(checkConnected > 1000){
      doStop();
  }else{
  
     motor1.setSpeed(strengthMotor1);
     motor2.setSpeed(strengthMotor2);
     motor3.setSpeed(strengthMotor3);
     motor4.setSpeed(strengthMotor4);

     Serial.println("motorDir1: "+String(motorDir1));
     Serial.println("motorDir2: "+String(motorDir2));                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
     Serial.println("motorDir3: "+String(motorDir3));
     Serial.println("motorDir4: "+String(motorDir4)); 

     motor1.run(motorDir1);
     motor2.run(motorDir2);
     motor3.run(motorDir3);
     motor4.run(motorDir4); 
  }
 

//  switch (data){
//    case 'F':
//     if(isSensorOn){
//        if(caculateDistanance() > 5){
//          setMotorStrengthForward(strength);
//          goForward();
//        }
//     }
//      
//      break;
//    case 'L':
//      goLeft(strength);
//      break;
//    case 'R':
//      goRight(strength);
//      break;
//    case 'S':
//      setMotorStrengthForward(0);
//      doStop();
//      break;
//    case 'B':
//      setMotorStrengthForward(strength);
//      goBack();
//      break;
//  }
//  
}

void setMotorStrengthForward(int s){
  s = abs(s);
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

void goLeft(int s){
  motor1.run(FORWARD);
  motor2.run(BACKWARD);
  motor3.run(FORWARD);
  motor4.run(BACKWARD);
}
void goRight(int s){
  motor1.run(BACKWARD);
  motor2.run(FORWARD);
  motor3.run(BACKWARD);
  motor4.run(FORWARD);
}

int getStrengthFromInput(char one, char two, char three){
  String oneString = String(one);
  String twoString = String(two);
  String threeString = String(three);
  String all = oneString + twoString + threeString;
  all.replace("X","");

  return all.toInt();; 
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
