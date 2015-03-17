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
    
    
    
    
    private static final int PACKET_LIFETIME = 200;
    
    
    
    //So that we don't need to re-initialize every time
    byte leftMotorPower;
	byte rightMotorPower;
	byte rearMotorPower;
	
	DriveDirection leftMotorDir;
	DriveDirection rightMotorDir;
	DriveDirection rearMotorDir;    
    

    public HolonomicRobotController(Radio radio, boolean initialCatcher){
        super(radio, initialCatcher);
    }

    @Override
    public void onMotionComplete() {

    }
    
    public DrivePacket stop(){
    	byte zero = 0;
    	
    	return new DrivePacket(
    			zero, leftMotorDir, 
    			zero, rightMotorDir, 
    			zero, rearMotorDir,
    			PACKET_LIFETIME);
    }
    
    
    
    public DrivePacket rotate(double angle){    	
    	
    	double a = 0.7, minPower = 100;
    	
    	double maxPower = 200;
    	
    	double rearMotorCoef = 0.5;
    	
    	
    	double power = a*Math.abs(angle) + minPower;
    	
    	power = Math.min(maxPower, power);
    	
    	//RobotDebugWindow.messageAttacker.setMessage("Rotating power: "+power);
    	
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

    	
    	return new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME);
    	
    }
    
    
    
    public DrivePacket travel(double displacement){
    	
    	double a = 1.5, b = 50;
    	
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
    	

    	return new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME);
    	
    }
    
    
public DrivePacket travelSideways(double displacement){
    	
		double a = 3, minPower = 150;
    	double power = a*Math.abs(displacement) +minPower;   	
    	
    	power = Math.min(254, power);
    	
    	
    	double arcCorrectionCoef = 0.2;
    	byte arcCorrectionPower = 100;
    	
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

    	return new DrivePacket(
    			leftMotorPower, leftMotorDir, 
    			rightMotorPower, rightMotorDir, 
    			rearMotorPower, rearMotorDir,
    			PACKET_LIFETIME);
    }
    
    
    
    public Packet openCatcher(){
    	return new DisengageCatcherPacket();
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
