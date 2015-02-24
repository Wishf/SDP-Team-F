package sdp.control;

import sdp.util.DriveDirection;

/**
 * Created by Matthew on 06/02/2015.
 */
public abstract class Maneuver extends RobotCommand {
    public abstract DriveDirection getMotorDirection(int i);

    public abstract byte getMotorPower(int i);

    public abstract int getDuration();

    @Override
    public boolean canQueue(){
        return true;
    }
}
