//Arduino Bluetooth Controlled Car
//Before uploading the code you have to install the necessary library
//Note - Disconnect the Bluetooth Module before hiting the upload button otherwise you'll get compilation error message.
//AFMotor Library https://learn.adafruit.com/adafruit-motor-shield/library-install 
//After downloading the library open Arduino IDE >> go to sketch >> Include Libray >> ADD. ZIP Libray >> Select the downloaded 
//ZIP File >> Open it >> Done
//Now You Can Upload the Code without any problem but make sure the bt module isn't connected with Arduino while uploading code

#include <AFMotor.h>

//initial motors pin
AF_DCMotor motor1(1, MOTOR12_1KHZ); 
AF_DCMotor motor2(2, MOTOR12_1KHZ); 
AF_DCMotor motor3(3, MOTOR34_1KHZ);
AF_DCMotor motor4(4, MOTOR34_1KHZ);

bool n = false;
uint8_t c = FORWARD;

char command; 

void setup() 
{       
  Serial.begin(9600);  //Set the baud rate to your Bluetooth module.
  n = false;
  c = "FORWARD";
}

void loop(){
//  if(Serial.available() > 0){
//   debil();
//  }
  if(Serial.available() > 0){ 
    command = Serial.read(); 
    Stop();
    switch(command){
    case 'F':  
      forward();
      break;
    case 'B':  
       back();
      break;
    case 'L':  
      left();
      break;
    case 'R':
      right();
      break;
    case '':
      forwardRight();
    case 'D':
        debil();    
    }
  } 
//  debil();
}
void debil(){
  motor1.setSpeed(255);
  motor1.run(c);
  motor2.setSpeed(255);
  motor2.run(c);
  motor3.setSpeed(255);
  motor3.run(c);
  motor4.setSpeed(255);
  motor4.run(c);
delay(2000);
if(n){
 n = false;
 c = BACKWARD; 
}else{
  n = true;
  c = FORWARD;
}
}
 void forwardB(){
  motor3.setSpeed(255);//Define maximum velocity
  motor3.run(BACKWARD); //rotate the motor clockwise
  motor4.setSpeed(255);//Define maximum velocity
  motor4.run(BACKWARD); //rotate the motor clockwise
  }
void forward()
{
  motor1.setSpeed(255); //Define maximum velocity
  motor1.run(BACKWARD); //rotate the motor clockwise
  motor2.setSpeed(255); //Define maximum velocity
  motor2.run(FORWARD); //rotate the motor clockwise
  motor3.setSpeed(255);//Define maximum velocity
  motor3.run(FORWARD); //rotate the motor clockwise
  motor4.setSpeed(255);//Define maximum velocity
  motor4.run(FORWARD); //rotate the motor clockwise
}
void forwardRight(){
  motor1.setSpeed(240); //Define maximum velocity
  motor1.run(FORWARD); //rotate the motor clockwise
  motor2.setSpeed(255); //Define maximum velocity
  motor2.run(FORWARD); //rotate the motor clockwise
  motor3.setSpeed(255);
  motor3.run(FORWARD);
  motor4.setSpeed(240);
  motor4.run(BACKWARD); 
}
void back()
{
  motor1.setSpeed(255); //Define maximum velocity
  motor1.run(FORWARD); //rotate the motor anti-clockwise
  motor2.setSpeed(255); //Define maximum velocity
  motor2.run(BACKWARD); //rotate the motor anti-clockwise
  motor3.setSpeed(255); //Define maximum velocity
  motor3.run(BACKWARD); //rotate the motor anti-clockwise
  motor4.setSpeed(255); //Define maximum velocity
  motor4.run(BACKWARD); //rotate the motor anti-clockwise
}

void left()
{
  motor1.setSpeed(255); //Define maximum velocity
  motor1.run(BACKWARD); //rotate the motor anti-clockwise
  motor2.setSpeed(255); //Define maximum velocity
  motor2.run(BACKWARD); //rotate the motor anti-clockwise
  motor3.setSpeed(255); //Define maximum velocity
  motor3.run(BACKWARD);  //rotate the motor clockwise
  motor4.setSpeed(255); //Define maximum velocity
  motor4.run(FORWARD);  //rotate the motor clockwise
}

void right()
{
  motor1.setSpeed(255); //Define maximum velocity
  motor1.run(FORWARD); //rotate the motor clockwise
  motor2.setSpeed(255); //Define maximum velocity
  motor2.run(FORWARD); //rotate the motor clockwise
  motor3.setSpeed(255); //Define maximum velocity
  motor3.run(FORWARD); //rotate the motor anti-clockwise
  motor4.setSpeed(255); //Define maximum velocity
  motor4.run(BACKWARD); //rotate the motor anti-clockwise
} 

void Stop(){
  motor1.setSpeed(0); //Define minimum velocity
  motor1.run(RELEASE); //stop the motor when release the button
  motor2.setSpeed(0); //Define minimum velocity
  motor2.run(RELEASE); //rotate the motor clockwise
  motor3.setSpeed(0); //Define minimum velocity
  motor3.run(RELEASE); //stop the motor when release the button
  motor4.setSpeed(0); //Define minimum velocity
  motor4.run(RELEASE); //stop the motor when release the button
}
