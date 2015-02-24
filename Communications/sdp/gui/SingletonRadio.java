package sdp.gui;

import sdp.comms.Radio;
import sdp.comms.packets.*;

import java.util.HashMap;

/**
 * Created by conrad on 27/01/15.
 */
public class SingletonRadio extends Radio {
    private static HashMap<String,Radio> radios = new HashMap<String,Radio>();
    private String port;

    public SingletonRadio(String port_) {
    	super(null);
        port = port_;
    	System.out.println("Starting on port " + port);
        if(!radios.containsKey(port_)) {
        	System.out.println("Creating radio for " + port);
            Radio rad = new Radio(port);
            rad.start();
            rad.sendPacket(new ActivatePacket());
            radios.put(port,rad);
        }
    }

    @Override
    public void sendPacket(Packet packet) {
        radios.get(port).sendPacket(packet);
    }
}
