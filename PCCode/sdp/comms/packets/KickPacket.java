package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

/**
 * Created by Matthew on 16/01/2015.
 */
public class KickPacket extends Packet {
    public static final byte ID = 'K';
    public static final int Length = 2;

    private byte power;

    public KickPacket(byte power) {
        this.power = power;
    }

    // Empty constructor
    public KickPacket(){}

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public void writePacket(SerialPort sendPort) throws SerialPortException {
        sendPort.writeByte(ID);
        sendPort.writeByte(power);
    }

    @Override
    public void readPacket(CircularByteBuffer stream) {
        try {
            power = stream.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
