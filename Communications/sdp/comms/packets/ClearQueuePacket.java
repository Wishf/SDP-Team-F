package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

/**
 * Created by Matthew on 03/02/2015.
 */
@Deprecated
public class ClearQueuePacket extends Packet {
    public static final byte ID = 'L';
    public static final int Length = 1;

    public ClearQueuePacket() {

    }

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
