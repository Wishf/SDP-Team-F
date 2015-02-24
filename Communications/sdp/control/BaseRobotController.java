package sdp.control;

import sdp.comms.PacketListener;
import sdp.comms.Radio;
import sdp.comms.packets.*;

/**
 * Created by Matthew on 06/02/2015.
 */
public abstract class BaseRobotController implements PacketListener {
    private boolean catcherEngaged;
    private MotionQueue motions;
    private Radio radio;

    public BaseRobotController(Radio radio, boolean initialCatcher) {
        catcherEngaged = initialCatcher;
        motions = new MotionQueue(16);
        this.radio = radio;
        //radio.addListener(this);
    }

    public boolean sendCommand(RobotCommand command) {
        if(command.canQueue()){
            Maneuver m = (Maneuver) command;
            if(motions.enqueue(m)) {
                radio.sendPacket(m.toPacket());
                return true;
            }

            return false;
        }

        radio.sendPacket(command.toPacket());
        return true;
    }

    public void popQueue() {
        motions.pop();
        radio.sendPacket(new PopQueuePacket());
    }

    public void clearQueue(){
        motions.clear();
        radio.sendPacket(new ClearQueuePacket());
    }

    @Override
    public void packetArrived(Packet p) {
        if(p instanceof MotionCompletePacket){
            onMotionComplete();
            motions.pop();
        } else if(p instanceof CatcherStateToggledPacket) {
            catcherEngaged = !catcherEngaged;
        }
    }

    public boolean getCatcherState(){
        return catcherEngaged;
    }

    public abstract void onMotionComplete();
}
