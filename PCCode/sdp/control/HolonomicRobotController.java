package sdp.control;

import sdp.comms.Radio;
import sdp.comms.packets.*;
import sdp.util.DriveDirection;
import sdp.vision.gui.tools.RobotDebugWindow;

/**
 * Created by Matthew on 09/02/2015.
 */
public class HolonomicRobotController extends BaseRobotController {

    private int[] msPerMm;
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
    private static final double WHEEL_RATIO = GROUP9_WHEEL_DIAMETER / (double)WHEEL_DIAMETER;

    public HolonomicRobotController(Radio radio,
                                    boolean initialCatcher,
                                    int msPerMm_wheel1,
                                    int msPerMm_wheel2,
                                    int msPerMm_wheel3){
        super(radio, initialCatcher);

        msPerMm = new int[3];
        msPerMm[0] = msPerMm_wheel1;
        msPerMm[1] = msPerMm_wheel2;
        msPerMm[2] = msPerMm_wheel3;
    }

    @Override
    public void onMotionComplete() {

    }
    
    //speed in degrees per second
    public DrivePacket rotate(double angle){    	
    	// Turning circle circumference
    	double circumference = Math.PI * (WHEEL_LEFT_DIST + WHEEL_RIGHT_DIST);
    	// How far we want each wheel to travel
    	double arcLength = (Math.abs(angle) / 360) * circumference;
    	
    	double a = 0.4, b = 90;
    	double power = a*arcLength + b;
    	power = Math.min(255, power);
    	
    	byte motor1Power = (byte) power;
    	byte motor2Power = (byte) power;
    	
    	DriveDirection leftMotorDir;
    	DriveDirection rightMotorDir;
    	
    	if(angle > 0){
    		
    		leftMotorDir = DriveDirection.FORWARD;			
    		rightMotorDir = DriveDirection.BACKWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	}

    	
    	RobotDebugWindow.messageAttacker.setMessage("Powers are: " + motor1Power + ", " + motor2Power);
    	return new DrivePacket(
    			motor1Power, leftMotorDir, 
    			motor2Power, rightMotorDir, 
    			(byte)0, rightMotorDir,
                150);
    	
    }
    
    
    
    public DrivePacket travel(double displacement){
    	
    	//System.out.println("Travel: "+distance);
    	
    	double a = 1.5, b = 50;
    	double power = a*displacement +b;
    	power = Math.min(255, power);
    	
    	//TODO: calculate the speed actually
    	byte motor1power = (byte) power;
    	byte motor2power = (byte) power;
    	
    	DriveDirection leftMotorDir;
    	DriveDirection rightMotorDir;
    	
    	if(displacement > 0){
    		leftMotorDir = DriveDirection.FORWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.BACKWARD;
    	}
    	
    	//System.out.println("motor1power:"+motor1power+"motor2power:"+motor2power);

    	return new DrivePacket(
    			motor1power, leftMotorDir, 
    			motor2power, rightMotorDir, 
    			(byte) 0, DriveDirection.FORWARD,
                150);
    	
    }
    
    
public DrivePacket travelSideways(double distance){
    	
		double absDistance = Math.abs(distance);
    	
    	double a = 0.7, b = 90;
    	double power = a*absDistance +b;
    	power = Math.min(255, power);
    	
    	double arcCorrectionCoef = 0.1;
    	
    	byte motor1power = (byte) (power*arcCorrectionCoef);
    	byte motor3power = (byte) power;
    	
    	DriveDirection leftMotorDir = DriveDirection.FORWARD;
    	DriveDirection rightMotorDir = DriveDirection.FORWARD;
    	DriveDirection rearMotorDir;
    	
    	if(distance > 0){
    		leftMotorDir = DriveDirection.FORWARD;
        	rightMotorDir = DriveDirection.BACKWARD;
    		rearMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
        	rightMotorDir = DriveDirection.FORWARD;
    		rearMotorDir = DriveDirection.BACKWARD;
    	}

    	return new DrivePacket(
    			motor1power, leftMotorDir, 
    			motor1power, rightMotorDir, 
    			motor3power, rearMotorDir,
                500);
    	
    }
    
    
    
    
    public DrivePacket travelArc(double arcRadius, int distance, int speed){
    	
    	int millis = Math.round(distance * msPerMm[0]);
    	
    	//TODO: calculate the speed actually
    	byte motor1power = (byte) 255;
    	byte motor2power = (byte) 255;
    	
    	DriveDirection leftMotorDir;
    	DriveDirection rightMotorDir;
    	
    	if(distance > 0){
    		leftMotorDir = DriveDirection.FORWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.BACKWARD;
    	}

    	return new DrivePacket(
    			motor1power, leftMotorDir, 
    			motor2power, rightMotorDir, 
    			(byte) 0, DriveDirection.FORWARD,
                millis);
    	
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
