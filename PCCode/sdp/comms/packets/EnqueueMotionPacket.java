package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.strategy.StrategyController;
import sdp.util.CircularByteBuffer;
import sdp.util.DriveDirection;

/**
 * Created by Matthew on 03/02/2015.
 */
@Deprecated
public class EnqueueMotionPacket extends Packet {
    public final static byte ID = 'Q';
    public final static int Length = 9;

    private byte[] motorPowers;
    private int millis;

    public EnqueueMotionPacket(
            byte motor1Power, DriveDirection motor1Direction,
            byte motor2Power, DriveDirection motor2Direction,
            byte motor3Power, DriveDirection motor3Direction,
            int millis) {
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
        
        if(this.millis > StrategyController.STRATEGY_TICK * 0.8)
        	this.millis = (int) (StrategyController.STRATEGY_TICK * 0.8);
        if(this.millis < 50)
        	this.millis = 50;

    }

    public EnqueueMotionPacket()
    {
    }

    @Override
    public byte getID() {
        return ID;
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
    
    @Override
    public String toString() {
    	
    	String str = "Motion: ";
    	
    	for(int i = 0; i < 6; i++) {
    		String byteString = Byte.toString(motorPowers[i]);
    		str += byteString + "; ";
    	}
    	
    	str += millis;
    	
    	return str;
    	
    	
    }
}
