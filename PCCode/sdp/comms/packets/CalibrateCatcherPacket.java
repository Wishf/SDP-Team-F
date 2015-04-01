package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

/**
 * Created by Matthew on 16/01/2015.
 */
public class CalibrateCatcherPacket extends Packet {
    public static final byte ID = 'R';
    public static final int Length = 1;

    // Empty constructor
    public CalibrateCatcherPacket(){}

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public void writePacket(SerialPort sendPort) throws SerialPortException {
        sendPort.writeByte(ID);
    }

    @Override
    public void readPacket(CircularByteBuffer stream) {        
    }
}
