package sdp.gui;

import sdp.comms.Radio;
import sdp.comms.packets.*;

/**
 * Created by conrad on 27/01/15.
 */
public class SingletonRadio extends Radio {
    private static Radio rad;

    public SingletonRadio(String port) {
    	super(null);
    	System.out.println("Starting");
        if(rad == null) {
        	System.out.println("Creating radio");
            rad = new Radio(port);
            rad.start();
            rad.sendPacket(new ActivatePacket());
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        rad.sendPacket(packet);
    }
}
