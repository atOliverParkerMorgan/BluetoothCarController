#include <AFMotor.h>

#define PI 3.14159265

AF_DCMotor motor1(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3, MOTOR12_64KHZ);
AF_DCMotor motor4(4, MOTOR12_64KHZ);


const int BUFFER_SIZE = 18;
int availableBitsInBuffer = -1;
char buf[BUFFER_SIZE];
int lastIndex = 0;
char command[BUFFER_SIZE];


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
  availableBitsInBuffer = Serial.available();
  
  if(availableBitsInBuffer > 0){
     checkConnected = 0;
     Serial.readBytes(buf, availableBitsInBuffer);

     for(int i = 0; i < availableBitsInBuffer; i++){
        command[lastIndex] = buf[i];
        if(lastIndex==BUFFER_SIZE-1){
           lastIndex = 0; 
          
           isSensorOn = command[0] == 'E';
           isAutomtic = command[1] == 'A';
           
           motorDir1 = RELEASE;
           if(command[2] == 'F'){
              motorDir1 = FORWARD;
           }else if(command[2] == 'B'){
              motorDir1 = BACKWARD;
           }
           strengthMotor1 = getStrengthFromInput(command[3], command[4], command[5]);
           
           motorDir2 = RELEASE;
           if(commandf[6] == 'F'){
              motorDir2 = FORWARD;
           }else if(command[6] == 'B'){
              motorDir2 = BACKWARD;
           }
           strengthMotor2 = getStrengthFromInput(command[7], command[8], command[9]);
           
           motorDir3 = RELEASE;
           if(command[10] == 'F'){
              motorDir3 = FORWARD;
           }else if(command[10] == 'B'){
              motorDir3 = BACKWARD;
           }
           strengthMotor3 = getStrengthFromInput(command[11], command[12], command[13]);
           
           motorDir4 = RELEASE;
           if(command[14] == 'F'){
              motorDir4 = FORWARD;
           }else if(command[14] == 'B'){
              motorDir4 = BACKWARD;
           }
           strengthMotor4 = getStrengthFromInput(command[15], command[16], command[17]);
           
           for( int i = 0; i < BUFFER_SIZE; i++){
                 Serial.print(String(command[i]));   
           } 
           Serial.println();
           Serial.println("Sensor:"+ String(isSensorOn));
           Serial.println("Automtic: "+String(isAutomtic));
           Serial.println("motor1: "+String(strengthMotor1));
           Serial.println("motor2: "+String(strengthMotor2));                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
           Serial.println("motor3: "+String(strengthMotor3));
           Serial.println("motor4: "+String(strengthMotor4));
       }
       lastIndex++;
     }

    

  }else if (checkConnected<=255){
     checkConnected++;
  }
  
  if(checkConnected > 255){
      doStop();
  }else{

     motor1.setSpeed(strengthMotor1);
     motor2.setSpeed(strengthMotor2);
     motor3.setSpeed(strengthMotor3);
     motor4.setSpeed(strengthMotor4);

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
