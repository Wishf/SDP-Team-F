#include "SDPArduino.h"
#include "CommsLib.h"
#include <Wire.h>


#define DEBUG 1

//Globals
bool ON = false;

//Moving
#define MOTOR_N 3
bool motorsChanged = false;
byte motorMapping[MOTOR_N] = {1, 0, 2};
int motorPower[MOTOR_N] = {0,0,0};
int motorDirs[MOTOR_N] = {1, 1, 1};
int motorMultiplier[MOTOR_N] = {1, 1, 1};
long motorTimeoutStart = 0;
long motorTimeoutMillis = 0;

//Kicking
#define KICK_DELAY_MOVING_UP 250
#define KICK_DELAY_UP 200
#define KICK_DELAY_MOVING_DOWN 220

#define KICK_STATE_IDLE 0
#define KICK_STATE_START 1
#define KICK_STATE_MOVING_UP 2
#define KICK_STATE_UP 3
#define KICK_STATE_MOVING_DOWN 4

#define KICK_MOTOR 4
#define KICK_MOTOR_DIR -1

long kickStartTime;
int kickState = KICK_STATE_IDLE;
int kickPower = 0;

//Catch
#define CATCH_MOTOR 3

#define CATCH_STATE_IDLE 0
#define CATCH_STATE_DISENGAGE 1
#define CATCH_STATE_ENGAGE 2
#define CATCH_STATE_OPERATING_ENGAGE 3
#define CATCH_STATE_OPERATING_DISENGAGE 4
//#define CATCH_STATE_ENGAGED 5
//#define CATCH_STATE_DISENGAGE 6

#define CATCH_DISENGAGE_DIR 1
#define CATCH_DISENGAGE_POWER 100
#define CATCH_DISENGAGE_DELAY 550

#define CATCH_ENGAGE_DIR -1
#define CATCH_ENGAGE_POWER 100
#define CATCH_ENGAGE_DELAY 550

long catchStartTime;
int catchState = CATCH_STATE_IDLE;

Communications comms;


void setup() {
  
  SDPsetup(); 
  motorAllStop();

  // Set up packet handlers
  comms.set_handler('D', deactivate);
  comms.set_handler('A', activate);
  comms.set_handler('K', kick);
  comms.set_handler('R', sensor_read);
  comms.set_handler('M', drive);
  comms.set_handler('N', engage_catcher);
  comms.set_handler('I', disengage_catcher);

  comms.print("started");// transmit started packet
}


//3 kick 2byte
//4 motor 4byte
//5 read sensor 2byte
void loop() {
  
  //Coms
  comms.loop();
 
  //Control 
  if(ON)
  {
    doMotors();
    doKick();
    doCatcher();
  }
  else
  {    
    motorAllStop();
  } 
}


void moveMotor(int motor, int power){
  /*
  debugPrint("\n***MM ");
  debugPrint(motor);
  debugPrint(", ");
  debugPrint(power);
  debugPrint(" **\n");
  */
  
  
  if(power >= 0){
    motorForward(motor, power);
  }
  else if(power < 0){
    motorBackward(motor, -power);
  }
  else {
    motorStop(motor);
  }
}

void doCatcher(){
  if(catchState == CATCH_STATE_ENGAGE){
    catchStartTime = millis();

    catchState = CATCH_STATE_OPERATING_ENGAGE;

    moveMotor(CATCH_MOTOR, CATCH_ENGAGE_POWER * CATCH_ENGAGE_DIR);
  }
  else if(catchState == CATCH_STATE_OPERATING_ENGAGE){
     if(millis() - catchStartTime >= CATCH_ENGAGE_DELAY){
       catchStartTime = millis();

       catchState = CATCH_STATE_IDLE;

       motorStop(CATCH_MOTOR);
     }
  }
  else if(catchState == CATCH_STATE_DISENGAGE){
    catchStartTime = millis();

    catchState = CATCH_STATE_OPERATING_DISENGAGE;

    moveMotor(CATCH_MOTOR, CATCH_DISENGAGE_POWER * CATCH_DISENGAGE_DIR);
  }
  else if(catchState == CATCH_STATE_OPERATING_DISENGAGE){
      if(millis() - catchStartTime >= CATCH_DISENGAGE_DELAY){
       catchStartTime = millis();

       catchState = CATCH_STATE_IDLE;

       motorStop(CATCH_MOTOR);
     }
  }
}

void doKick(){  
  if(kickState == KICK_STATE_START){
    kickStartTime = millis();
    
    kickState = KICK_STATE_MOVING_UP;
    
    moveMotor(KICK_MOTOR, kickPower * KICK_MOTOR_DIR);
  }
  else if(kickState == KICK_STATE_MOVING_UP){
    if(millis() - kickStartTime > KICK_DELAY_MOVING_UP){
      kickStartTime = millis();
      
      kickState = KICK_STATE_UP;
    
      motorStop(KICK_MOTOR); 
    }
  }
  else if(kickState == KICK_STATE_UP){
    if(millis() - kickStartTime > KICK_DELAY_UP){
      kickStartTime = millis();
      
      kickState = KICK_STATE_MOVING_DOWN;
      
      moveMotor(KICK_MOTOR, -kickPower * KICK_MOTOR_DIR);
    }
  } 
  else if(kickState == KICK_STATE_MOVING_DOWN)
    if(millis() - kickStartTime > KICK_DELAY_MOVING_DOWN){
      kickState = KICK_STATE_IDLE;
      
      motorStop(KICK_MOTOR); 
  } 
}

void doMotors(){
  if(motorsChanged){
    motorsChanged = false;
    motorTimeoutStart = millis();

    int i = 0;
    for( ; i < MOTOR_N; i++)
    {
       if(motorMapping[i] > -1){
         moveMotor(motorMapping[i], motorPower[i] * motorDirs[i] * motorMultiplier[i]);
       }
    }
  }
  else if (motorTimeoutMillis > 0 && motorTimeoutStart > 0){
    long difference = millis() - motorTimeoutStart;

    int i = 0;
    if(difference >= motorTimeoutMillis){
      for( ; i < MOTOR_N; i++)
      {
         if(motorMapping[i] > -1){
           comms.print("disabled");
           moveMotor(motorMapping[i], 0);
         }
      }
      motorTimeoutMillis = 0;
      motorTimeoutStart = 0;
    }
  }
}

// Responds to code 'D'
void deactivate(){
  comms.send('C');
  comms.print("deactivated");

  ON = false;
}

// Responds to code 'A'
void activate() {
  comms.send('C');
  comms.print("activated");

  ON = true;
}

// Responds to code 'K'
void kick() {
    comms.send('C');
    comms.print("kick");

    byte nextByte = comms.read_byte();

    if(kickState == KICK_STATE_IDLE){
      kickState = KICK_STATE_START;
      kickPower = nextByte;
    }

    comms.print(" ");
    comms.print(kickPower);
}

// Responds to code 'R'
// Is currently incomplete; doesn't work properly
void sensor_read() {
  comms.print("read");
  byte nextByte = comms.read_byte();
  //Read sensor
  //Reply
  comms.print(" ");
  comms.print(nextByte);


  //comms.send('S');
  //comms.send("1234");
}

// Responds to code 'M'
void drive() {
  comms.send('C');
  comms.print("motors ");

  int i = 0;
  for( ; i < MOTOR_N; i++)
  {
    byte nextByte = comms.read_byte();
    motorPower[i] = nextByte;


    nextByte = comms.read_byte();
    if(nextByte == 0)
      motorDirs[i] = 1;
    else
      motorDirs[i] = -1;


    comms.print(motorPower[i]);
    comms.print(" ");
  }

  motorTimeoutMillis = comms.read_unsigned_short();

  motorsChanged = true;
}

// Responds to code 'N'
void engage_catcher(){
  comms.send('G');
  comms.print("catch");

  if(catchState == CATCH_STATE_IDLE){
    catchState = CATCH_STATE_ENGAGE;
  }
}

// Respomnds to code 'I'
void disengage_catcher(){
  comms.send('G');
  comms.print("uncatch");

  if(catchState == CATCH_STATE_IDLE){
    catchState = CATCH_STATE_DISENGAGE;
  }
}
