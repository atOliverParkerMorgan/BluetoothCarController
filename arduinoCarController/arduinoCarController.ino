#include <AFMotor.h>

#define PI 3.14159265
#define unit 0.01;


AF_DCMotor motor1(1, MOTOR12_64KHZ);
AF_DCMotor motor2(2, MOTOR12_64KHZ);
AF_DCMotor motor3(3, MOTOR12_64KHZ);
AF_DCMotor motor4(4, MOTOR12_64KHZ);
String input = "";
int strength = 0;

// angles are mesured in cosinus value 
// 1 - right 
// -1 - left
float angle = 0;
float currentAngle = PI/2;
char readData[10]="0000000000";

// commands
// S - stop
// F - forward
// B - back
// L - left
// R - right

char currentCommand = 'S';

void setup(){
    Serial.begin(9600);  //Set the baud rate to your Bluetooth module.
   
    Serial.println("HERE");
    setMotorStrength(100);
}

void loop(){
  if(Serial.available() > 0){
     Serial.readBytes(readData, 10);
     
     String data = String(readData);
     int indexOfS = data.indexOf('s');
     angle = ( PI / 180.0 ) * data.substring(1, indexOfS).toInt();
     strength = data.substring(indexOfS+1, data.indexOf('!')).toInt();
     //Serial.println(strength);
    }

  if(strength == 0){
      currentCommand = 'S';  
  }
  else if(angle-currentAngle>0.02 ){
     
    Serial.println(currentAngle);
       currentCommand = 'L';
       currentAngle+=unit;
       if(currentAngle>2*PI){
          currentAngle = 0;
       }
  }else if(currentAngle-angle>0.02){
    
    Serial.println(currentAngle);
    currentCommand = 'R';
    currentAngle-=unit;
     if(currentAngle<0){
          currentAngle = 2*PI;
     }

  }else{
    currentCommand = 'F';
  }
  
//  }else if(angle>currentAngle and strength>10){
//     Serial.println("RIGHTHHH");
//     currentCommand = 'R';
//     currentAngle+=0.01; 
//      
//  }else if(strength > 10){
//     currentCommand = 'F';
//  }else{
//     currentCommand = 'S';
//  }
 
    
     
 

  switch (currentCommand){
    case 'F':
      goForward();
      break;
    case 'L':
      goLeft();
      break;
    case 'R':
      goRight();
      break;
    case 'S':
      Stop();
      break;
  }
}

void setMotorStrength(int s){
  motor1.setSpeed(s);
  motor2.setSpeed(s);
  motor3.setSpeed(s);
  motor4.setSpeed(s);

}
void Stop(){
  motor1.run(RELEASE);
  motor2.run(RELEASE);
  motor3.run(RELEASE);
  motor4.run(RELEASE);
}

void goForward(){
  motor1.run(BACKWARD);
  motor2.run(BACKWARD);
  motor3.run(BACKWARD);
  motor4.run(BACKWARD);
}

void goRight(){
  motor1.run(FORWARD); //rotate the motor anti-clockwise
  motor2.run(BACKWARD); //rotate the motor anti-clockwise
  motor3.run(BACKWARD);  //rotate the motor clockwise
  motor4.run(FORWARD);  //rota  
}
void goLeft(){
  motor1.run(BACKWARD); //rotate the motor anti-clockwise
  motor2.run(FORWARD); //rotate the motor anti-clockwise
  motor3.run(FORWARD);  //rotate the motor clockwise
  motor4.run(BACKWARD);  //rota  
}
