package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

/**
 * Created by Matthew on 06/02/2015.
 */
public class DisengageCatcherPacket extends Packet {
    public final static byte ID = 'I';
    public final static byte Length = 1;

    public DisengageCatcherPacket(){}

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
