package sdp.comms;

import sdp.comms.packets.Packet;

/**
 * Created by Matthew on 06/02/2015.
 */
public interface PacketListener {
    public void packetArrived(Packet p);
}
