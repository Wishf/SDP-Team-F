package sdp.control;

import sdp.comms.Radio;
import sdp.comms.packets.*;
import sdp.util.DriveDirection;
import sdp.vision.gui.tools.RobotDebugWindow;

/**
 * Created by Matthew on 09/02/2015.
 */
public class HolonomicRobotController extends BaseRobotController {

    /*private int[] msPerMm;
    // Grey holonomic wheels are 58mm in diameter
    // http://www.microrobo.com/58mm-lego-compatible-omni-wheel.html
    private static final int WHEEL_DIAMETER = 58;
    private static final double WHEEL_CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER;
    private static final double WHEEL_DEG_PER_DIST = (double)(360 / WHEEL_CIRCUMFERENCE);
    // Distances from center of wheel to center of axle (back wheel measured from middle of front axle; probably wrong)
    private static final double WHEEL_LEFT_DIST = 57.5;
    private static final double WHEEL_RIGHT_DIST = 57.5;
    private static final double WHEEL_BACK_DIST = 80;

    // Front axle track width
    private static final double TRACK_WIDTH = WHEEL_LEFT_DIST + WHEEL_RIGHT_DIST;
    private static final double TURN_RATIO = TRACK_WIDTH / WHEEL_DIAMETER;

    private static final int GROUP9_WHEEL_DIAMETER = 57;
    private static final double GROUP9_WHEEL_CIRCUMFERENCE = Math.PI * GROUP9_WHEEL_DIAMETER;

    // Ratio to convert speed from group 9 to speed fitting our wheels
    private static final double WHEEL_RATIO = GROUP9_WHEEL_DIAMETER / (double)WHEEL_DIAMETER;*/
    
    
    private byte ARC_POWER = 100;
    private double ROTATE_REAR_COEF = 0.5;
    public double ROTATE_MIN = 100;
    public double TRAVEL_MIN = 150;
    public double SIDEWAYS_MIN = 200;
    
    private static final int PACKET_LIFETIME = 200;
    
    
    
    //So that we don't need to re-initialize every time
    byte leftMotorPower;
	byte rightMotorPower;
	byte rearMotorPower;
	
	DriveDirection leftMotorDir;
	DriveDirection rightMotorDir;
	DriveDirection rearMotorDir;    
    

    public HolonomicRobotController(Radio radio, byte position){
        super(radio, position);
        
        leftMotorDir = DriveDirection.FORWARD;
        rightMotorDir = DriveDirection.FORWARD;
        rearMotorDir = DriveDirection.FORWARD;
    }

    @Override
    public void onMotionComplete() {

    }
    
    public boolean stop(){
    	byte zero = 0;
    	
    	return this.sendCommand(new DrivePacket(
    			zero, leftMotorDir, 
    			zero, rightMotorDir, 
    			zero, rearMotorDir,
    			PACKET_LIFETIME));
    }
    
    
    
    public boolean rotate(double angle){    	
    	
    	double a = 0.1, minPower = ROTATE_MIN, c = 0;
    	
    	double maxPower = 254;
    	
    	double rearMotorCoef = ROTATE_REAR_COEF;
    	
    	
    	double vp = Math.min(1, Math.abs(this.angularVelocity)/this.maxAngularVelocity);
    	
    	double power = a*Math.abs(angle) + minPower - vp*c;
    	
    	power = Math.min(maxPower, power);
    	
    	System.out.println("Rotating power: "+power);
    	
    	leftMotorPower = (byte) power;
    	rightMotorPower = (byte) power;
    	rearMotorPower = (byte) (power * rearMotorCoef);
    	
    	
    	
    	
    	if(angle > 0){    		
    		leftMotorDir = DriveDirection.FORWARD;			
    		rightMotorDir = DriveDirection.BACKWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	}
    	rearMotorDir = rightMotorDir;

    	
    	return this.sendCommand(new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME));
    	
    }
    
    
    
    public boolean travel(double displacement){
    	
    	double a = 1.5, b = TRAVEL_MIN;
    	
    	double power = a*Math.abs(displacement) +b;
    	power = Math.min(254, power);
    	
    	
    	leftMotorPower = (byte) power;
    	rightMotorPower = (byte) power;
    	rearMotorPower = 0;
    	

    	
    	if(displacement > 0){
    		leftMotorDir = DriveDirection.FORWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.BACKWARD;
    	}
    	rearMotorDir = DriveDirection.FORWARD;
    	

    	return this.sendCommand(new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME));
    	
    }
    
  
public boolean travelSideways(double displacement){
    	
		double a = 3, minPower = SIDEWAYS_MIN;
    	double power = a*Math.abs(displacement) +minPower;   	
    	
    	power = Math.min(254, power);
    	
    	
    	double arcCorrectionCoef = 0.2;
    	byte arcCorrectionPower = (byte)ARC_POWER;
    	
    	leftMotorPower = (byte) arcCorrectionPower;//(power*arcCorrectionCoef);
    	rightMotorPower = (byte) arcCorrectionPower;//(power*arcCorrectionCoef);
    	rearMotorPower = (byte) power;
    	
    	
    	
    	if(displacement > 0){
    		leftMotorDir = DriveDirection.FORWARD;
        	rightMotorDir = DriveDirection.BACKWARD;
    		rearMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
        	rightMotorDir = DriveDirection.FORWARD;
    		rearMotorDir = DriveDirection.BACKWARD;
    	}

    	return this.sendCommand(new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME));
    }
    
    
	public void travelAtPower(int power){
		leftMotorPower = (byte) Math.abs(power);
    	rightMotorPower = (byte) Math.abs(power);
    	rearMotorPower = 0;
    	

    	
    	if(power > 0){
    		leftMotorDir = DriveDirection.FORWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.BACKWARD;
    	}
    	rearMotorDir = DriveDirection.FORWARD;
    	

    	this.sendCommand(new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME));
	}
	
	public void rotateAtPower(byte power){
		leftMotorPower = (byte) Math.abs(power);
    	rightMotorPower = (byte) Math.abs(power);
    	rearMotorPower = (byte)(Math.abs(power) * ROTATE_REAR_COEF);
    	

    	
    	if(power > 0){    		
    		leftMotorDir = DriveDirection.FORWARD;			
    		rightMotorDir = DriveDirection.BACKWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	}
    	rearMotorDir = rightMotorDir;

    	
    	this.sendCommand(new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME));
	}
	
	public void travelSideways(byte power){
		leftMotorPower = (byte) ARC_POWER;//(power*arcCorrectionCoef);
    	rightMotorPower = (byte) ARC_POWER;//(power*arcCorrectionCoef);
    	rearMotorPower = (byte) Math.abs(power);
    	
    	
    	
    	if(power > 0){
    		leftMotorDir = DriveDirection.FORWARD;
        	rightMotorDir = DriveDirection.BACKWARD;
    		rearMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
        	rightMotorDir = DriveDirection.FORWARD;
    		rearMotorDir = DriveDirection.BACKWARD;
    	}

    	this.sendCommand(new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME));
	}
	
    public boolean openCatcher(){
    	return this.sendCommand(new DisengageCatcherPacket());
    }
    
    public boolean closeCatcher(){
    	return this.sendCommand(new EngageCatcherPacket());
    }
    
    public boolean kick(){
    	return this.sendCommand(new KickPacket());
    }
    
    
    
    
    /**
     * Not yet implemented posibly never will be, don't account for it
     * @param angle
     * @param distance
     * @return
     */
	public Packet travelDiagonally(double angle, double distance) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
