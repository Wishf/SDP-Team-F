#Communications Documentation
This file details the purposes and workings of the files in the /sdp/comms folder.

##Radio.java
This class represents an RF connection between the PC and an Arduino. When using 2 RF dongles to communicate with 2 Arduinos, you will also need 2 instances of the Radio class.

###Properties
Each instance of the radio class has three properties:

- SerialPort port : the serial port that the dongle is connected to, like */dev/ttyACM0*.
- List&lt;PacketListener> listenerList : the list of listeners - things that will receive data transmitted back over the connection.

###Methods
There are also a number of fairly self explanatory methods

- getPortNames() : displays a list of the available serial ports.
- start() : opens the connection for this instance of Radio.
- stop() : closes the connection for this instance of Radio.
- sendPacket(Packet packet) : send the specified packet over the connection for this instance of Radio.
- addListener(PacketListener listener) : add a listener to this instance of Radio.

##RadioController.java
This class represents something magical.

##Packets
Each instruction for the robot to complete has its own packet, such as *KickPacket* or *DrivePacket*. All of the specific action packets extend *Packet* which provides a basis for the other packets. Some add other parameters such as kicking power or an array of motor powers for moving.
Each packet type shares the same basic structure, but some may have more properties.

###Properties

- ID : The ID used for each action, such as 'K' for kicking.
- Length : The number of bytes that this packet uses.

###Methods
- getID() : returns the ID corresponding to that packet - this corresponds to the instruction like "kick".
- writePacket(SerialPort sendPort) : writes the packet over the specified serial port to the Arduino.
- readPacket(CircularByteBuffer stream) : reads any information that we may pass back from the robot, interpreting it for this packet. For example, *EngageCatcherPacket* has no extra parameters so has nothing that could be read. *KickPacket* on the other hand has a power for how hard we want the kick to be, so the readPacket method here will read whatever's on the stream and interpret that as the power level for a kick.

##RobotCommand.java
This is the class that the strategy will use to build the packets it needs to send to the robot to make it move or kick for example. Each command like Stop is a tiny class that extends GenericCommand, meaning that Kick will build a new *KickPacket*, set the power level to the parameter it's given, and then send it over the radio to the Arduino.
