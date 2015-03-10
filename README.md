# SDP-Team-F
SDP Team F - Team Space Dragon

##PC Setup
###Using Eclipse;
Create new Java Project (arbitrary name)  
Import -> File Structure -> Tick the complete project  
Right-Click 'PCCode' in Package Explorer -> 'Build Path' -> 'Use as source folder'  
Should only need to fix library imports at this point.  
Package Explorer -> Libraries -> Select all .jar files -> Right-Click, Add to Build Path  
At this point there should be no compilation errors.  
Right-Click RunVision.java -> Run As -> Run Configurations -> Environment -> New...  
Name: LD_LIBRARY_PATH  
Value: ${project_loc}/Libraries  
Apply and run.

##OR##

If you want to import the project into eclipse without importing the files into /workspace/ (so that you will be able to still be able commit and push your changes from the git folder)...

Create new Project
Select Java Project
Untick 'Use Default Location'
Browse... to location where you cloned the project.
Next... Make sure the PCCode is the source folder
Right-Click RunVision.java -> Run As -> Run Configurations -> Environment -> New...  
Name: LD_LIBRARY_PATH  
Value: ${project_loc}/Libraries  
Apply and run.


###Vision Settings
Whenever you open the vision control panel, click 'Settings' -> 'Load Settings'  
It does not automatically load previously saved settings otherwise.

##Repository Structure
###Arduino
This directory contains all of the code that runs on the Arduino.

###PCCode
This directory contains all of the code that runs on the PC.
