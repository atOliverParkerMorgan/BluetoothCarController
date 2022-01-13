//Arduino Bluetooth Controlled Car
//Before uploading the code you have to install the necessary library
//Note - Disconnect the Bluetooth Module before hiting the upload button otherwise you'll get compilation error message.
//AFMotor Library https://learn.adafruit.com/adafruit-motor-shield/library-install 
//After downloading the library open Arduino IDE >> go to sketch >> Include Libray >> ADD. ZIP Libray >> Select the downloaded 
//ZIP File >> Open it >> Done
//Now You Can Upload the Code without any problem but make sure the bt module isn't connected with Arduino while uploading code

#include <Servo.h>
#include <Adafruit_MotorShield.h>


bool n = false;
uint8_t c = FORWARD;

int command;
const int ROTATION_RATE = 1;
int angle = 90;

// Create the motor shield object with the default I2C address
Adafruit_MotorShield AFMS = Adafruit_MotorShield(); 

Adafruit_StepperMotor *myStepper = AFMS.getStepper(200, 1);
// We'll also test out the built in Arduino Servo library
Servo servo1;
// Select which 'port' M1, M2, M3 or M4. In this case, M1
Adafruit_DCMotor *myMotor = AFMS.getMotor(1);
// You can also make another motor on port M2
Adafruit_DCMotor *myOtherMotor = AFMS.getMotor(2);

void setup() 
{       
  Serial.begin(9600);  //Set the baud rate to your Bluetooth module.
  n = false;
  c = "FORWARD";
  if (!AFMS.begin()) {         // create with the default frequency 1.6KHz
  // if (!AFMS.begin(1000)) {  // OR with a different frequency, say 1KHz
    Serial.println("Could not find Motor Shield. Check wiring.");
    while (1);
  }

  Serial.println("Motor Shield found.");

  // Attach a servo to pin #10
  servo1.attach(10);

  // turn on motor M1
  myMotor->setSpeed(200);
  myMotor->run(FORWARD);

  // setup the stepper
  myStepper->setSpeed(10);  // 10 rpm
}

void loop(){
  
//  if(Serial.available() > 0){
//   debil();
//  }
//  if(Serial.available() > 0){ 
//    command = Serial.read();
//    command -= 90;
//    Stop();
//    Serial.println("COMMAND: "+command);
//    Serial.println("ANGLE: "+angle);
//    if(command<angle){
//      angle-=ROTATION_RATE;
//      left();
//    }else if(command<angle){
//      angle+=ROTATION_RATE;
//      right();
//    }else{
//      forward();
//    }
//  }
//  debil();
}
//void debil(){
//  motor1.setSpeed(255);
//  motor1.run(c);
//  motor2.setSpeed(255);
//  motor2.run(c);
//  motor3.setSpeed(255);
//  motor3.run(c);
//  motor4.setSpeed(255);
//  motor4.run(c);
//delay(2000);
//if(n){
// n = false;
// c = BACKWARD; 
//}else{
//  n = true;
//  c = FORWARD;
//}
//}
// void forwardB(){
//  motor3.setSpeed(255);//Define maximum velocity
//  motor3.run(BACKWARD); //rotate the motor clockwise
//  motor4.setSpeed(255);//Define maximum velocity
//  motor4.run(BACKWARD); //rotate the motor clockwise
//  }
//void forward()
//{
//  motor1.setSpeed(255); //Define maximum velocity
//  motor1.run(BACKWARD); //rotate the motor clockwise
//  motor2.setSpeed(255); //Define maximum velocity
//  motor2.run(FORWARD); //rotate the motor clockwise
//  motor3.setSpeed(255);//Define maximum velocity
//  motor3.run(FORWARD); //rotate the motor clockwise
//  motor4.setSpeed(255);//Define maximum velocity
//  motor4.run(FORWARD); //rotate the motor clockwise
//}
//void forwardRight(){
//  motor1.setSpeed(240); //Define maximum velocity
//  motor1.run(FORWARD); //rotate the motor clockwise
//  motor2.setSpeed(255); //Define maximum velocity
//  motor2.run(FORWARD); //rotate the motor clockwise
//  motor3.setSpeed(255);
//  motor3.run(FORWARD);
//  motor4.setSpeed(240);
//  motor4.run(BACKWARD); 
//}
//void back()
//{
//  motor1.setSpeed(255); //Define maximum velocity
//  motor1.run(FORWARD); //rotate the motor anti-clockwise
//  motor2.setSpeed(255); //Define maximum velocity
//  motor2.run(BACKWARD); //rotate the motor anti-clockwise
//  motor3.setSpeed(255); //Define maximum velocity
//  motor3.run(BACKWARD); //rotate the motor anti-clockwise
//  motor4.setSpeed(255); //Define maximum velocity
//  motor4.run(BACKWARD); //rotate the motor anti-clockwise
//}
//
//void left()
//{
//  motor1.setSpeed(255); //Define maximum velocity
//  motor1.run(BACKWARD); //rotate the motor anti-clockwise
//  motor2.setSpeed(255); //Define maximum velocity
//  motor2.run(BACKWARD); //rotate the motor anti-clockwise
//  motor3.setSpeed(255); //Define maximum velocity
//  motor3.run(BACKWARD);  //rotate the motor clockwise
//  motor4.setSpeed(255); //Define maximum velocity
//  motor4.run(FORWARD);  //rotate the motor clockwise
//}
//
//void right()
//{
//  motor1.setSpeed(255); //Define maximum velocity
//  motor1.run(FORWARD); //rotate the motor clockwise
//  motor2.setSpeed(255); //Define maximum velocity
//  motor2.run(FORWARD); //rotate the motor clockwise
//  motor3.setSpeed(255); //Define maximum velocity
//  motor3.run(FORWARD); //rotate the motor anti-clockwise
//  motor4.setSpeed(255); //Define maximum velocity
//  motor4.run(BACKWARD); //rotate the motor anti-clockwise
//}
//
//void Stop(){
//  motor1.setSpeed(0); //Define minimum velocity
//  motor1.run(RELEASE); //stop the motor when release the button
//  motor2.setSpeed(0); //Define minimum velocity
//  motor2.run(RELEASE); //rotate the motor clockwise
//  motor3.setSpeed(0); //Define minimum velocity
//  motor3.run(RELEASE); //stop the motor when release the button
//  motor4.setSpeed(0); //Define minimum velocity
//  motor4.run(RELEASE); //stop the motor when release the button
//}
