package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

import java.nio.charset.Charset;


public class BallStatePacket extends Packet {
    public static final byte ID = 'Y';
    public static final int Length = 2;

    public boolean ballCaught;
    public BallStates state;

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
        byte[] buf = new byte[1];
        try {
            stream.read(buf, 0, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String s = new String(buf, Charset.forName("US-ASCII"));

        switch(s.charAt(0)){
            case '0': // catcher open
                ballCaught = false;
                state = BallStates.OPEN;
                break;
            case '1': // catcher opening
                ballCaught = false;
                state = BallStates.OPENING;
                break;
            case '2': // has ball
                ballCaught = true;
                state = BallStates.CLOSE;
                break;
            case '3': // catcher closing
                ballCaught = false;
                state = BallStates.CLOSING;
                break;
        }
    }
}