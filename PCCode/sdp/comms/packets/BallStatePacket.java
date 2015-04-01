package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

/**
 * Created by Matthew on 01/04/2015.
 */
public class BallStatePacket extends Packet {
    public static final byte ID = 'Y';
    public static final int Length = 2;

    public boolean ballCaught;

    public BallStatePacket(){

    }

    public BallStatePacket(boolean caught) {
        ballCaught = caught;
    }

    @Override
    public byte getID() {
        return ID;
    }

    @Override
    public void writePacket(SerialPort sendPort) throws SerialPortException {
        sendPort.writeByte(ID);
        if(ballCaught){
            sendPort.writeByte((byte)1);
        } else {
            sendPort.writeByte((byte)0);
        }
    }

    @Override
    public void readPacket(CircularByteBuffer stream) {
        byte val = 0;
        try {
            val = stream.read();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ballCaught = val != 0;
    }
}