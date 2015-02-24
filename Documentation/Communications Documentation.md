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
