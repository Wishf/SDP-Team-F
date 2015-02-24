package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

/**
 * Created by Matthew on 16/01/2015.
 */
public class ReadSensorPacket extends Packet {
    public static final byte ID = 'R';
    public static final int Length = 2;

    private byte sensorID;

    public ReadSensorPacket(byte sensorID) {
        this.sensorID = sensorID;
    }

    // Empty constructor
    public ReadSensorPacket(){}

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public void writePacket(SerialPort sendPort) throws SerialPortException {
        sendPort.writeByte(ID);
        sendPort.writeByte(sensorID);
    }

    @Override
    public void readPacket(CircularByteBuffer stream) {
        try {
            sensorID = stream.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
