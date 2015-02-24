package sdp.util;

/**
 * Created by Matthew on 29/01/2015.
 */
public enum DriveDirection {
    FORWARD ((byte)0),
    BACKWARD ((byte)1);

    private final byte encodedValue;
    DriveDirection(byte encoding) {
        encodedValue = encoding;
    }

    public byte getEncoding(){
        return encodedValue;
    }

}
