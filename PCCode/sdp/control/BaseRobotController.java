package sdp.control;

import sdp.comms.PacketListener;
import sdp.comms.Radio;
import sdp.comms.packets.*;

/**
 * Created by Matthew on 06/02/2015.
 */
public abstract class BaseRobotController implements PacketListener {
    private boolean catcherEngaged;
    private Radio radio;

    public BaseRobotController(Radio radio, boolean initialCatcher) {
        catcherEngaged = initialCatcher;
        this.radio = radio;
        //radio.addListener(this);
    }

    public boolean sendCommand(RobotCommand command) {
        if(command.canQueue()){
            Maneuver m = (Maneuver) command;
            radio.sendPacket(m.toPacket());
            return true;
        }

        radio.sendPacket(command.toPacket());
        return true;
    }


    @Override
    public void packetArrived(Packet p) {
        if(p instanceof MotionCompletePacket){
            onMotionComplete();
        } else if(p instanceof CatcherStateToggledPacket) {
            catcherEngaged = !catcherEngaged;
        }
    }

    public boolean getCatcherState(){
        return catcherEngaged;
    }

    public abstract void onMotionComplete();
}
