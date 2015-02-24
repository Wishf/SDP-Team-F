package sdp.control;

import sdp.comms.packets.Packet;

/**
 * Created by Matthew on 09/02/2015.
 */
public abstract class RobotCommand {
    public abstract Packet toPacket();

    public abstract boolean canQueue();
}
