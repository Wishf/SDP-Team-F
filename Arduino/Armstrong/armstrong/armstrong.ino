#include <SDPArduino.h>

#include "CommsLib.h"

#include <Wire.h>


#define DEBUG false

#define ROTARY_SLAVE_ADDRESS 5
#define ROTARY_COUNT 6

//Globals
bool ON = true;

//Moving
#define MOTOR_N 3

// This variable is true if any of the motors are running
bool motorsIdle = false;

// This variable is true if a packet has been recieved that changes the state of the motors
bool motorsChanged = false;

// This is the ports that the main drive motors are assigned to
// The first port is the right drive motor (facing the kicker)
// The second port is the left drive motor (facing the kicker)
// The third port is the back drive motor
// For Armstrong (red transparent bricks), this should be [1,0,2]
// For John Longley (green transparent bricks), this should be [1,2,3]
byte motorMapping[MOTOR_N] = {
  0, 1, 3};

// The power from 0-255 to drive each motor at
int motorPower[MOTOR_N] = {
  0, 0, 0};

// The direction each motor is to be driven at
int motorDirs[MOTOR_N] = {
  1, 1, 1};

// The power multiplier for each motor, in case we need to drive certain motors harder
int motorMultiplier[MOTOR_N] = {
  1, 1, 1};

// The time in milliseconds since the Arduino was turned on/reset when the most recent motion command arrived
long motorTimeoutStart = 0;

// The time in milliseconds that the most recent motion command should run for at most
long motorTimeoutMillis = 0;

// To do with the tachometer board: DO NOT CHANGE
// The I2C address of the tachometer board Arduino
#define ROTARY_SLAVE_ADDRESS 5
// The number of rotary sensors (tachometer or encoders) that can be attached to it
#define ROTARY_COUNT 6

// KICKING
// State definitions for the kicker state machine
#define KICK_STATE_IDLE 0
#define KICK_STATE_START 1
#define KICK_STATE_MOVING_UP 2

// The motor port the kicker motor is attached to
#define KICK_MOTOR 5

// The tachometer port the kicker rotary sensor is attached to
#define KICK_TACHOMETER 4

// The number of rotary encoder ticks it takes to turn the kicker 1/4 of the way around it's axis
#define KICK_TICKS_QUARTER 5

// The direction to drive the motor to turn the kicker mechanism
#define KICK_MOTOR_DIR -1

// The current state the kicker state machine is
int kickState = KICK_STATE_IDLE;
// The number of tachometer ticks measured when the kick motion is started
int kickerTachometerStart = 0;
long kickStartTime;







// CATCH
bool hasBall = false;
// The motor port the catcher motor is attached to
#define CATCH_MOTOR 4

// State definitions for the catcher state machine
#define CATCH_STATE_DISENGAGED 0
#define CATCH_STATE_ENGAGED 1

#define CATCH_STATE_DISENGAGE 2
#define CATCH_STATE_OPERATING_DISENGAGE 3
#define CATCH_STATE_WINDDOWN_DISENGAGE 4
#define CATCH_STATE_ENGAGE 5
#define CATCH_STATE_OPERATING_ENGAGE 6
#define CATCH_STATE_WINDDOWN_ENGAGE 7

#define CATCH_WINDDOWN_PERIOD 2


#define CATCHER_TACHO 5
// The direction the motor has to be driven to disengage the catcher
#define CATCH_DISENGAGE_DIR 1
// The power to drive the motor at to disengage the catcher
#define CATCH_DISENGAGE_POWER 255
#define CATCH_DISENGAGE_HOLD_POWER 0
// The length of time the motor should be driven for to disengage the catcher
#define CATCH_DISENGAGE_DELAY 500


// The direction the motor has to be driven to engage the catcher
#define CATCH_ENGAGE_DIR -1
// The power to drive the motor at to engage the catcher
#define CATCH_ENGAGE_POWER 255
#define CATCH_ENGAGE_HOLD_POWER 0
// The length of time the motor should be driven for to engage the catcher
#define CATCH_ENGAGE_DELAY 500

// The time in milliseconds since the Arduino started up/reset that the catch engage/disengage motion was started
long catchStartTime;
int catchWindDownSpeed;

int catchTachoOpen;
int catchTachoClosed;
#define CATCH_TACHO_CAUGHT_THRESHOLD_MIN 1
#define CATCH_TACHO_CAUGHT_THRESHOLD_MAX 4

// Start by calibrating
int catchState = CATCH_STATE_DISENGAGE;
bool catcherCalibrated = false;






Communications comms;


void setup() {

  SDPsetup(); 
  motorAllStop();

  // Set up packet handlers
  // This means that for a given letter, a given function will be called
  // E.g. Recieving D should call deactivate
  comms.set_handler('D', deactivate);
  comms.set_handler('A', activate);
  comms.set_handler('K', kick);
  comms.set_handler('M', drive);
  comms.set_handler('N', engage_catcher);
  comms.set_handler('I', disengage_catcher);  
  comms.set_handler('Z', kicker_inc);
  comms.set_handler('X', kicker_dec);
  comms.set_handler('T', test);
  comms.set_handler('R', calibrate_catcher);


  comms.set_handler('B', has_ball);
  comms.set_handler('C', catcher_state);


  comms.print("started");// transmit started packet
}


// Main loop
void loop() {

  // Read tachometer values for this 'frame'
  updateMotorPositions();

  // Check communications and start handling one packet
  // This can set up state machines for motion to begin
  comms.loop();

  // If we're accepting motion packets
  // Update state machines for each kind of motion for one 'frame'
  if(ON)
  {
    // Update drive motor motions
    doMotors();
    // Update kicker state machine and motion
    doKick();
    // Update catcher state machine and motion
    doCatcher();
  }
  else
  {
    // Stop all motors if we're not accepting motion packets
    if(!motorsIdle){  
      motorAllStop();
      motorsIdle = true;
    }
  } 
}





// Move a given motor (indexed by port on the motor board) by a given power
// Negative values are backwards, positive are forward
// Zero will stop the motor
void moveMotor(int motor, int power){
  if(power > 0){
    motorForward(motor, power);
    motorsIdle = false;
  }
  else if(power < 0){
    motorBackward(motor, -power);
    motorsIdle = false;
  }
  else {
    motorStop(motor);
  }
}


// Manage catcher state machine and motor motions
void doCatcher(){
  // We are engaging the catcher
  if(catchState == CATCH_STATE_ENGAGE){
    // Record motion start time and set state to engage in operation
    catchStartTime = millis();

    catchState = CATCH_STATE_OPERATING_ENGAGE;

    // Start driving motors for engage
    moveMotor(CATCH_MOTOR, CATCH_ENGAGE_POWER * CATCH_ENGAGE_DIR);
  }
  // The catcher is being driven to engage
  else if(catchState == CATCH_STATE_OPERATING_ENGAGE){
    // If the catcher engage timeout has elapsed
    if(millis() - catchStartTime >= CATCH_ENGAGE_DELAY){
      // Set state to idle and stop motor
      catchState = CATCH_STATE_WINDDOWN_ENGAGE;
      
      //comms.println("Windown");
      
      catchWindDownSpeed = CATCH_ENGAGE_POWER;
      catchStartTime = millis();
      
    }
  }
  else if(catchState == CATCH_STATE_WINDDOWN_ENGAGE){

    if(millis() - catchStartTime >= CATCH_WINDDOWN_PERIOD){
      catchWindDownSpeed -= 1;
      catchStartTime = millis();
    }

    // If still winding down
    if(catchWindDownSpeed > 0){
      //comms.println(catchWindDownSpeed);
      //comms.print('\n');
      moveMotor(CATCH_MOTOR, catchWindDownSpeed * CATCH_ENGAGE_DIR);
    }
    else {

      if(!catcherCalibrated){
        catchTachoClosed = tacho(CATCHER_TACHO);
        catchState = CATCH_STATE_DISENGAGE;
        comms.print("Calib closed catcher tacho: ");
        comms.println(catchTachoClosed);
        catcherCalibrated = true;
      }
      else{        
        comms.print("Catch tacho diff from closed: ");
        comms.println(abs(catchTachoClosed - tacho(CATCHER_TACHO)));
        //Ball caught
        if((abs(catchTachoClosed - tacho(CATCHER_TACHO)) > CATCH_TACHO_CAUGHT_THRESHOLD_MIN) && (abs(catchTachoClosed - tacho(CATCHER_TACHO)) < CATCH_TACHO_CAUGHT_THRESHOLD_MAX)){
          // Set state to idle and stop motor
          catchState = CATCH_STATE_ENGAGED;
          moveMotor(CATCH_MOTOR, CATCH_ENGAGE_HOLD_POWER * CATCH_ENGAGE_DIR);
          hasBall = true;
          comms.println("Ball caught!");
        }
        else{
          catchState = CATCH_STATE_DISENGAGE; 
          comms.println("Ball not caught!");               
        }
        
        //Send back ball state
        has_ball();     
      }
      
    }
  }



  // We are disengaging the catcher
  else if(catchState == CATCH_STATE_DISENGAGE){
    // Record motion start time and set state to disengage in operation
    hasBall = false;
    catchStartTime = millis();

    catchState = CATCH_STATE_OPERATING_DISENGAGE;

    // Start driving motors for disengage
    moveMotor(CATCH_MOTOR, CATCH_DISENGAGE_POWER * CATCH_DISENGAGE_DIR);
  }
  // The catcher is being driven to disengage
  else if(catchState == CATCH_STATE_OPERATING_DISENGAGE){
    // If the catcher disengage timeout has elapsed
    if(millis() - catchStartTime >= CATCH_DISENGAGE_DELAY){
      // Start the winddown
      catchState = CATCH_STATE_WINDDOWN_DISENGAGE;

      //comms.println("Winddown");
      
      catchWindDownSpeed = CATCH_DISENGAGE_POWER;
    }
  }
  else if(catchState == CATCH_STATE_WINDDOWN_DISENGAGE){

    if(millis() - catchStartTime >= CATCH_WINDDOWN_PERIOD){
      catchWindDownSpeed -= 1;
      catchStartTime = millis();
    }

    // If still winding down
    if(catchWindDownSpeed > 0){
      //comms.print(catchWindDownSpeed);
      //comms.print('\n');
      moveMotor(CATCH_MOTOR, catchWindDownSpeed * CATCH_DISENGAGE_DIR);
    }
    else {
      // Set state to idle and stop motor
      catchState = CATCH_STATE_DISENGAGED;
      moveMotor(CATCH_MOTOR, CATCH_DISENGAGE_HOLD_POWER * CATCH_DISENGAGE_DIR);

      if(!catcherCalibrated){
        catchTachoOpen = tacho(CATCHER_TACHO);
        catchState = CATCH_STATE_ENGAGE;
        comms.print("Calib open catcher tacho: ");
        comms.println(catchTachoOpen);
      }
    }
  }
}


// Manage kicker state machine and motor motions
void doKick(){
  // We are starting a kick motion
  if(kickState == KICK_STATE_START){
    //Release catcher
    catchState = CATCH_STATE_DISENGAGE;
    
    
    // Take the initial tachometer reading at the start of this motion
    kickerTachometerStart = tacho(KICK_TACHOMETER);
    kickStartTime = millis();

    // Set state machine to signify the kicker is rotating
    kickState = KICK_STATE_MOVING_UP;

    // Start rotation
    moveMotor(KICK_MOTOR, 255 * KICK_MOTOR_DIR);
  }
  // We are in the middle of rotating the kicker
  else if(kickState == KICK_STATE_MOVING_UP){
    // If we've rotated the kicker a quarter of the way around
    //comms.print((millis()-kickStartTime));
    //comms.print(" ");
    //comms.println(abs(tacho(KICK_TACHOMETER) - kickerTachometerStart));

    



    if(abs(tacho(KICK_TACHOMETER) - kickerTachometerStart) > KICK_TICKS_QUARTER){
      // Set state to idle
      kickState = KICK_STATE_IDLE;

      // Stop kicker motor
      motorStop(KICK_MOTOR); 
    }
  }
}


// This function updates the motor state machine, handling timeout and expiry of motions, as well as controlling
// the drive motors
void doMotors(){
  // If we've received a packet to say that the motors should do a new motion, we record the start time of the motion
  if(motorsChanged){
    motorsChanged = false;
    motorTimeoutStart = millis();
  }

  int i = 0;

  for( ; i < MOTOR_N; i++)
  {
    // If the difference between the current time and the start time is greater than the timeout value, stop the motors
    if(millis() - motorTimeoutStart > motorTimeoutMillis){
      if(motorMapping[i] > -1){
        moveMotor(motorMapping[i], 0);
      }
    }
    // Else, Continue to drive the motors at their specified power and direction
    else{ 
      if(motorMapping[i] > -1){
        moveMotor(motorMapping[i], motorPower[i] * motorDirs[i] * motorMultiplier[i]);
      }
    }
  }
}

// TACHOMETER

// Tachometer positions
int positions[ROTARY_COUNT] = {
  0};

// Read tachometer readings for a given motor; indexed by port on the tachometer board
// This function was created to abstract details before we had the tachometer code
int tacho(int motor)
{
  return positions[motor];
}

// Get tachometer readings from tachometer board; taken from example code
void updateMotorPositions() {
  // Request motor position deltas from rotary slave board
  Wire.requestFrom(ROTARY_SLAVE_ADDRESS, ROTARY_COUNT);

  // Update the recorded motor positions
  for (int i = 0; i < ROTARY_COUNT; i++) {
    positions[i] += (int8_t) Wire.read();  // Must cast to signed 8-bit type
  }
}


// COMMUNICATIONS


// Responds to code 'D'
// Tells robot to refuse movement packets
void deactivate(){
  comms.send('C');
  comms.println("deactivated");

  ON = false;
}


// Responds to code 'A'
// Tells robot to accept movement packets
void activate() {
  comms.send('C');
  comms.println("activated");

  ON = true;

  // Make sure the catcher is open on initialise
  catchState = CATCH_STATE_DISENGAGE;
}


// Responds to code 'K'
// Completes a full kick cycle
void kick() {
  comms.send('C');
  comms.println("kick");

  // Start kicker rotation
  if(kickState == KICK_STATE_IDLE){
    kickState = KICK_STATE_START;
  }
}


// Responds to code 'M'
// Activates motors at a given power and direction for at most a given number of milliseconds
void drive() {
  // Send acknowledgement
  comms.send('C');
  comms.print("motors ");

  int i = 0;
  for( ; i < MOTOR_N; i++)
  {
    // Read power value
    byte nextByte = comms.read_byte();
    motorPower[i] = nextByte;

    // Read direction byte
    nextByte = comms.read_byte();

    // 0 = Forward
    // Anything else = Backwards
    if(nextByte == 0)
      motorDirs[i] = 1;
    else
      motorDirs[i] = -1;

    // Send back power value for debug if debug is enabled
    comms.print(motorPower[i]);
    comms.print(" ");
  }

  // Remaining two bytes are timeout value in milliseconds
  // That means you can have from 0 to 2^16 milliseconds of timeout
  motorTimeoutMillis = comms.read_unsigned_short();

  motorsChanged = true;
}


// Responds to code 'N'
// Close catcher pincers
void engage_catcher(){
  // Send response that signifies the catcher state has been changed
  comms.send('G');
  comms.println("catch");

  // Switch catcher state to start engage sequence
  //if(catchState != CATCH_STATE_OPERATING_DISENGAGE){
    catchState = CATCH_STATE_ENGAGE;
  //}
}

// Responds to code 'I'
// Open catcher pincers
void disengage_catcher(){
  // Send response that signifies the catcher state has been changed
  comms.send('G');
  comms.println("uncatch");

  // Switch catcher state to start disengage sequence
  //if(catchState == CATCH_STATE_IDLE){
    catchState = CATCH_STATE_DISENGAGE;
  //}
}




void kicker_inc(){
  comms.println("K+");
  kickerTachometerStart = tacho(KICK_TACHOMETER);
  moveMotor(KICK_MOTOR, 255 * KICK_MOTOR_DIR);
  while(abs(tacho(KICK_TACHOMETER) - kickerTachometerStart) < 1){
    updateMotorPositions();
  }
  motorStop(KICK_MOTOR);
}



void kicker_dec(){
  comms.println("K-");
  kickerTachometerStart = tacho(KICK_TACHOMETER);
  moveMotor(KICK_MOTOR, -255 * KICK_MOTOR_DIR);
  while(abs(tacho(KICK_TACHOMETER) - kickerTachometerStart) < 1){
    updateMotorPositions();
  }
  motorStop(KICK_MOTOR);
}



void test(){

  int i = 0;
  long time = 1000;
  comms.println("TEST\n");
  
  comms.println("LEFT FORWARD 125\n");  
  moveMotor(motorMapping[0], 125*motorDirs[0]);
  delay(time);
  
  comms.println("LEFT BACKWARD 125\n");  
  moveMotor(motorMapping[0], -125*motorDirs[0]);
  delay(time);
  
  motorStop(motorMapping[0]);
  
  
  
  comms.println("RIGHT FORWARD 125\n");  
  moveMotor(motorMapping[1], 125*motorDirs[1]);
  delay(time); 
  
  comms.println("RIGHT BACKWARD 125\n");  
  moveMotor(motorMapping[1], -125*motorDirs[1]);
  delay(time);
  
  motorStop(motorMapping[1]);
  
  
  
  
  comms.println("BACK FORWARD 125\n");  
  moveMotor(motorMapping[2], 125*motorDirs[2]);
  delay(time);
  
  comms.println("BACK BACKWARD 125\n");  
  moveMotor(motorMapping[2], -125*motorDirs[2]);
  delay(time);
  
  
  motorStop(motorMapping[2]);
  
  
  comms.println("TEST DONE!\n");
  
}


void has_ball(){
  comms.send('Y');
  if(hasBall){
    comms.send(1);
  }
  else{
    comms.send(0);
  }
}

void catcher_state(){
  if(catchState == CATCH_STATE_DISENGAGED){
    comms.send('0');
  }
  else{
    comms.send('1');
  }
}


void calibrate_catcher(){
  catchState = CATCH_STATE_DISENGAGE;
  catcherCalibrated = false;
  comms.send('C');
}

