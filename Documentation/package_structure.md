Package Structure
=================

__Directory Root__
- __Arduino__ - Arduino code
  - __Armstrong__ - For now, the code running on the attacker robot Armstrong (Group 1's robot)
  - __MotionQueue__ - Old library written to allow motions to be queued up on the Arduino; no plans to use it
  - __SDPArduino__ - Copy of the motor driver library given to us by the uni
- __Documentation__ - Self explanatory
- __Libraries__ - Also self explanatory
- __PCCode__ - PC-side code to be run on DICE
  - __constants__ - Constants defining the pitch area and camera calibrations used by last year's group 9 code
  - __sdp__ - The main Java package for all the SDP code
    - __comms__ - Communications code
    - __control__ - Robot control code
    - __gui__ - User interface tools (currently only contains ones not written by group 9)
    - __logging__ - Group 9's logging code
    - __physics__ - Physics models and simulation (not sure if this is still being worked on)
    - __strategy__ - Strategy code
    - __test__ - Group 9's tests
    - __util__ - Classes which don't fit into any specific category but are used
    - __vision__ - Vision system code
    - __world__ - World state system code