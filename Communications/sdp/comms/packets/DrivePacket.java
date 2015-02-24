package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;
import sdp.util.DriveDirection;

import java.nio.ByteBuffer;

/**
 * Created by Matthew on 16/01/2015.
 */

@Deprecated
public class DrivePacket extends Packet {
    public final static byte ID = 'M';
    public final static int Length = 7;

    private byte[] motorPowers;

    public DrivePacket(byte motor1Power, DriveDirection motor1Direction,
                       byte motor2Power, DriveDirection motor2Direction,
                       byte motor3Power, DriveDirection motor3Direction) {
        motorPowers = new byte[6];
        motorPowers[0] = motor1Power;
        motorPowers[1] = motor1Direction.getEncoding();
        motorPowers[2] = motor2Power;
        motorPowers[3] = motor2Direction.getEncoding();
        motorPowers[4] = motor3Power;
        motorPowers[5] = motor3Direction.getEncoding();
    }

    // Empty constructor
    public DrivePacket(){ motorPowers = new byte[6]; }

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public void writePacket(SerialPort sendPort) throws SerialPortException {
        sendPort.writeByte(ID);
        sendPort.writeBytes(motorPowers);
    }

    @Override
    public void readPacket(CircularByteBuffer stream) {
        try {
            stream.read(motorPowers,0,6);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
