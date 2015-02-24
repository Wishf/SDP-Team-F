package sdp.comms.packets;

import jssc.SerialPort;
import jssc.SerialPortException;
import sdp.util.CircularByteBuffer;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Created by Matthew Summers on 16/01/2015.
 */
public abstract class Packet {
    public static final byte ID = 0x00;
    public static final int Length = 0;

    public abstract byte getID();

    public abstract void writePacket(SerialPort sendPort) throws SerialPortException;

    public abstract void readPacket(CircularByteBuffer stream);
}
