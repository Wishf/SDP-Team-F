package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.strategy.StrategyController;
import sdp.util.CircularByteBuffer;
import sdp.util.DriveDirection;

/**
 * Created by Matthew on 16/01/2015.
 */


public class DrivePacket extends Packet {
    public final static byte ID = 'M';
    public final static int Length = 9;

    private byte[] motorPowers;
    private int millis;

    public DrivePacket(byte motor1Power, DriveDirection motor1Direction,
                       byte motor2Power, DriveDirection motor2Direction,
                       byte motor3Power, DriveDirection motor3Direction, int millis) {

        // Because Java is silly and doesn't allow unsigned types, we restrict the range of the millis
        // parameter to 0-65536 (an unsigned 16 bit integer) via truncation
        if(millis < 0) {
            millis = 0;
        } else if(millis > 65535) {
            millis = 65535;
        }

        motorPowers = new byte[6];
        motorPowers[0] = motor1Power;
        motorPowers[1] = motor1Direction.getEncoding();
        motorPowers[2] = motor2Power;
        motorPowers[3] = motor2Direction.getEncoding();
        motorPowers[4] = motor3Power;
        motorPowers[5] = motor3Direction.getEncoding();

        this.millis = millis;

        
    }

    // Empty constructor
    public DrivePacket(){ motorPowers = new byte[6]; }

    @Override
    public byte getID() {
        return ID;
    }
    
    public byte getPower(int motor){
    	return motorPowers[motor*2];
    }
    
    public DrivePacket getReverse(){
    	byte[] pows = new byte[3];
    	pows[0] = motorPowers[0];
    	pows[1] = motorPowers[2];
    	pows[2] = motorPowers[4];
    	DriveDirection[] dirs = new DriveDirection[3];
    	for(int i = 0; i < dirs.length; i++){
    		if(motorPowers[i*2+1] == DriveDirection.FORWARD.getEncoding()){
    			dirs[i] = DriveDirection.BACKWARD;
    		} else {
    			dirs[i] = DriveDirection.FORWARD;
    		}
    	}
    	return new DrivePacket(pows[0], dirs[0], pows[1], dirs[1], pows[2], dirs[2], millis);
    }

    @Override
    public void writePacket(SerialPort sendPort) throws SerialPortException {
        sendPort.writeByte(ID);
        sendPort.writeBytes(motorPowers);
        // Split int into 16 bit unsigned short
        // Take top half
        sendPort.writeByte((byte)((millis >> 8) & 0xFF));
        // Then bottom half
        sendPort.writeByte((byte)(millis & 0xFF));
    }

    @Override
    public void readPacket(CircularByteBuffer stream) {
        try {
            stream.read(motorPowers,0,6);
            byte topByte = stream.read();
            byte bottomByte = stream.read();
            millis = ((int)topByte << 8) & (int)bottomByte;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
