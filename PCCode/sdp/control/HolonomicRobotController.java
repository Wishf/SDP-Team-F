package sdp.control;

import sdp.comms.Radio;
import sdp.comms.packets.*;
import sdp.util.DriveDirection;

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
    public DrivePacket rotate(int angle, int speed){
    	System.out.println("Rotate: "+angle);
    	
    	double turnAngle = angle * TURN_RATIO;
    	
    	
    	
    	double powerScale = 0.9;//Math.min(1, angle+90/180);
    	//TODO: calculate the speed actually
    	byte motor1power = (byte) (150*powerScale);
    	byte motor2power = (byte) (150*powerScale);
    	byte motor3power = (byte) (150*powerScale);
    	
    	DriveDirection leftMotorDir;
    	DriveDirection rightMotorDir;
    	
    	if(angle > 0){
    		leftMotorDir = DriveDirection.FORWARD;
    		rightMotorDir = DriveDirection.BACKWARD;
    	} else {
    		leftMotorDir = DriveDirection.BACKWARD;
    		rightMotorDir = DriveDirection.FORWARD;
    	}
    	
    	return new DrivePacket(
    			motor1power, leftMotorDir, 
    			motor2power, rightMotorDir, 
    			motor3power, rightMotorDir,
                500);
    }
    
    
    
    public DrivePacket travel(int distance, int travelSpeed){
    	
    	System.out.println("Travel: "+distance);
    	int millis = Math.abs(Math.round(distance * msPerMm[0]));
    	
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
                5000);
    	
    }
    
    
public DrivePacket travelSideways(int distance, int travelSpeed){
    	
    	System.out.println("Travel sideways: "+distance+" "+travelSpeed);
    	int millis = Math.abs(Math.round(distance * msPerMm[2]));
    	
    	//TODO: calculate the speed actually
    	double powerScaling = 1;//Math.min(1, Math.abs(distance) / 50);
    	
    	byte motor1power = (byte) 0;
    	byte motor2power = (byte) 0;
    	byte motor3power = (byte) Math.max(100, (255 * powerScaling));
    	
    	System.out.println(motor3power);
    	
    	DriveDirection leftMotorDir = DriveDirection.FORWARD;
    	DriveDirection rightMotorDir = DriveDirection.FORWARD;
    	DriveDirection rearMotorDir;
    	
    	if(distance > 0){
    		rearMotorDir = DriveDirection.FORWARD;
    	} else {
    		rearMotorDir = DriveDirection.BACKWARD;
    	}

    	return new DrivePacket(
    			motor1power, leftMotorDir, 
    			motor2power, rightMotorDir, 
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
    
    
    public DrivePacket move(double dX, double dY, double dA){
    	
    	double c1 = 1;
    	double c2 = 1;
    	double c3 = 1;
    	
    	double vX = Math.min(1, Math.max(-1, Math.pow(dX*c1, 3)));
    	double vY = Math.min(1, Math.max(-1, Math.pow(dY*c2, 3)));
    	double vA = Math.min(1, Math.max(-1, Math.pow(dA*c3, 3)));
    	
    	
    	double v1 = vX + vA;
    	double v2 = vY + vA;
    	double v3 = vX + vA;
    	
    	
    	
    	byte motor1power, motor2power, motor3power;
    	DriveDirection motor1dir, motor2dir, motor3dir;
    	
    	
    	
    	return null;
    	
    	/*
    	return new DrivePacket(
    			motor1power, motor1dir, 
    			motor2power, motor2dir,
    			motor3power, motor3dir);
    			*/
    }
    
}
