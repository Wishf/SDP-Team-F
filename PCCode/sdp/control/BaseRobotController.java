package sdp.control;

import sdp.comms.PacketListener;
import sdp.comms.Radio;
import sdp.comms.RobotCommand;
import sdp.comms.packets.*;
import sdp.vision.gui.tools.RobotDebugWindow;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.WorldState;

/**
 * Created by Matthew on 06/02/2015.
 */
public abstract class BaseRobotController implements PacketListener {
    private boolean catcherEngaged;
    private Radio radio;
    private byte position;
    
    private MovingObject oldRobot, freshRobot;
    
    protected double linearVelocity = 0, angularVelocity = 0;
    protected double maxForwardsVelocity = 0.1, maxSidewaysVelocity = 0.1, maxAngularVelocity = 0.7;
    
    private long lastSetWorldState = -1;

    public BaseRobotController(Radio radio, byte position) {
        this.radio = radio;
        this.position = position;
        this.catcherEngaged = true;
    }

    public boolean sendCommand(Packet packet) {
        radio.sendPacket(packet);
        return true;
    }
    
    public void setCalibration(double maxForwardsVelocity, double maxSidewaysVelocity, double maxAngularVelocity){
    	this.maxAngularVelocity = maxAngularVelocity;
    	this.maxForwardsVelocity = maxForwardsVelocity;
    	this.maxSidewaysVelocity = maxSidewaysVelocity;
    }
    
    public void setWorldState(WorldState worldState){
    	
    	
    	if(lastSetWorldState > 0){
    		long delta = System.currentTimeMillis() - lastSetWorldState; 
    		
    		if(delta < 5){
    			return;
    		}
    		
    		
    		
    		lastSetWorldState = System.currentTimeMillis();
    		
    		if(position == (byte)'d'){
    			oldRobot = freshRobot;
    			freshRobot = worldState.getDefenderRobot().copy();
    		}
    		else {
    			oldRobot = freshRobot;
    			freshRobot = worldState.getAttackerRobot().copy();
    		}
    		
    		this.angularVelocity = (freshRobot.orientation_angle - oldRobot.orientation_angle)/(double)delta;
    		
    		double dx = freshRobot.x - oldRobot.x;
    		double dy = freshRobot.y - oldRobot.y;
    		this.linearVelocity = Math.sqrt(dx*dx + dy*dy)/(double)delta;    	
    		
    		//" "+Math.round(this.linearVelocity*1000)+
    		//System.out.println(delta+" "+Math.round(this.angularVelocity*1000));
    		//RobotDebugWindow.messageAttacker.setMessage(this.linearVelocity+" "+this.angularVelocity);
    	}else{       		
    		lastSetWorldState = System.currentTimeMillis();
    		if(position == (byte)'d'){
    			freshRobot = worldState.getDefenderRobot().copy();
    		}
    		else {
    			freshRobot = worldState.getAttackerRobot().copy();
    		}
    	}
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
