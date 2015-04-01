package sdp.control;

import java.text.DecimalFormat;

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
    protected byte position;
    protected boolean moved = false;
    protected boolean rotated = false;
    private MovingObject oldRobot, freshRobot;
    protected DrivePacket lastPacket = null;
    
    protected double linearVelocity = 0, vX=0, vY=0, angularVelocity = 0;
    protected double maxForwardsVelocity = 0.1, maxSidewaysVelocity = 0.08, maxAngularVelocity = .25;
    
    protected double vForwards=0, vSideways=0;
    
    
    private long lastSetWorldState = -1;
    
    
    private double smoothing = 0.92;
    private DecimalFormat df = new DecimalFormat("+0.00000;-0.00000"); 
    protected double lastLinearVelocity = 0, lastXV=0, lastYV=0, lastAngularVelocity = 0;
    
    

    public BaseRobotController(Radio radio, byte position) {
        this.radio = radio;
        this.position = position;
        this.catcherEngaged = true;
        
        this.oldRobot = new MovingObject();
        this.freshRobot = new MovingObject();
    }

    public boolean sendCommand(Packet packet) {
    	if(packet instanceof DrivePacket) lastPacket = (DrivePacket)packet;
    	else lastPacket = null;
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
    		
    		if(position == (byte)'d'){
    			freshRobot = worldState.getDefenderRobot().copyTo(freshRobot);
    		}
    		else {
    			freshRobot = worldState.getAttackerRobot().copyTo(freshRobot);
    		}
    		
    		
    		//Check if changed(since it's all double values, direct comparison is pretty much guaranteed to work correctly)
    		if(!freshRobot.equals(oldRobot)){
    			long delta = System.currentTimeMillis() - lastSetWorldState;     			
    			lastSetWorldState = System.currentTimeMillis();
    			
    			//Probably not needed anymore
    			if(delta < 10){
        			return;
        		}
    			
    			
    			
        			
        		
    			
        		double raw_angle = freshRobot.orientation_angle - oldRobot.orientation_angle;
        		double angle;  
        		
        		angle = processAngle(raw_angle);
        		
        		//System.out.println(df.format(freshRobot.orientation_angle)+"    "+df.format(oldRobot.orientation_angle)+"    "+df.format(raw_angle)+"    "+df.format(angle));
        		
        		
        		
        		this.angularVelocity = exponentialSmoothing(smoothing, lastAngularVelocity, angle/(double)delta);
        		
        		
        		
        		if(angle > 10) {
        			//System.out.println("!!!!!!ROTATED!!!!!!! " + angle);
        			rotated = true;
    			}
        		else{ 
        			rotated = false;
        		}
        		
        		vX = exponentialSmoothing(smoothing, lastXV, (freshRobot.x - oldRobot.x)/(double)delta);
        		vY = exponentialSmoothing(smoothing, lastYV, (freshRobot.y - oldRobot.y)/(double)delta);
        		
        		
        		double theta = freshRobot.orientation_angle;
        		this.vForwards = vX*Math.cos(theta) + vY*Math.sin(theta);
        		this.vSideways = -vX*Math.sin(theta) + vY*Math.cos(theta);
        		
        		
        		
        		this.linearVelocity = Math.sqrt(vX*vX + vY*vY);
        		
        		
        		
        		if(position == (byte)'d'){
        			//RobotDebugWindow.messageDefender.setMessage("vx="+df.format(this.xV)+"; vy="+df.format(this.yV)+"; av="+df.format(this.angularVelocity));
        		}
        		else {
        			//RobotDebugWindow.messageAttacker.setMessage("vx="+df.format(this.xV)+"; vy="+df.format(this.yV)+"; av="+df.format(this.angularVelocity));
        		}
        		
        		
        		//" "+Math.round(this.linearVelocity*1000)+
        		//System.out.println(rotated);
        		//RobotDebugWindow.messageAttacker.setMessage(this.linearVelocity+" "+this.angularVelocity);
        		
        		lastXV = vX;
        		lastYV = vY;
        		lastLinearVelocity = linearVelocity;
				lastAngularVelocity = angularVelocity;
        		oldRobot = freshRobot.copyTo(oldRobot);
    		}
    	}else{       		
    		lastSetWorldState = System.currentTimeMillis();
    		if(position == (byte)'d'){
    			freshRobot = worldState.getDefenderRobot().copyTo(freshRobot);
    		}
    		else {
    			freshRobot = worldState.getAttackerRobot().copyTo(oldRobot);
    		}
    		oldRobot = freshRobot.copyTo(oldRobot);
    	}
    	
    }
    
    private double processAngle(double angle){
    	if(angle > 180){
    		angle -= 360;
    	}
    	else if(angle < -180){
    		angle += 360;
    	}
    	
    	return angle;
    }
    
    private double exponentialSmoothing(double a, double lastVal, double curVal){
    	return a*lastVal + (1-a)*curVal;
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