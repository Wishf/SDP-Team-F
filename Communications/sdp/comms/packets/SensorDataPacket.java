package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

import java.nio.ByteBuffer;

/**
 * Created by Matthew Summers on 16/01/2015.
 */
public class SensorDataPacket extends Packet {
    public static final byte ID = 'S';
    public static final int Length = 5;

    private int data;

    public SensorDataPacket(int data) {
        this.data = data;
    }

    // Empty constructor
    public SensorDataPacket(){}

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
