package sdp.control;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

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
    
    
    
    
    
    public double ROTATE_MIN = 70;
    public double ROTATE_MAX = 200;
    public double ROTATE_A = 0.01;
    public double ROTATE_REAR_COEF = 0.5;
    public double ROTATE_INITIAL_BOOST_COEF = 1.3; 
    
    public double TRAVEL_MIN = 120;
    public double TRAVEL_MAX = 254;
    public double TRAVEL_A = 1;    
    
    public double SIDEWAYS_MIN = 150;
    public double SIDEWAYS_MAX = 254;
    public double SIDEWAYS_A = 1;
    public byte SIDEWAYS_ARC_POWER = 70;
    
    
    private static final int PACKET_LIFETIME = 150;
    
    
    
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
    	if(lastPacket == null){
    		return this.sendCommand(new DrivePacket(
    				zero, leftMotorDir, 
    				zero, rightMotorDir, 
    				zero, rearMotorDir,
    				PACKET_LIFETIME));
    	}else{
    		return this.sendCommand(lastPacket.getReverse());
    		/*try{Thread.sleep(PACKET_LIFETIME);
    			this.sendCommand(new DrivePacket(
    				zero, leftMotorDir, 
    				zero, rightMotorDir, 
    				zero, rearMotorDir,
    				PACKET_LIFETIME));}catch(Exception e){e.printStackTrace();}*/
    		//return true;
    	}
    }
    
    
    
    public boolean rotate(double angle){    	
    	
    	double a = ROTATE_A, minPower = ROTATE_MIN, c = 0;
    	
    	double maxPower = ROTATE_MAX;
    	
    	double rearMotorCoef = ROTATE_REAR_COEF;
    	
    	if(!rotated){
    		minPower *= ROTATE_INITIAL_BOOST_COEF;
    	}
    	
    	double vp = Math.min(1, Math.abs(this.angularVelocity)/this.maxAngularVelocity);
    	
    	double power = a*Math.abs(angle) + minPower - vp*c;
    	
    	power = Math.min(maxPower, power);
    	
    	//System.out.println("Rotating power: "+power);
    	
    	leftMotorPower = (byte) power;
    	rightMotorPower = (byte) power;
    	rearMotorPower = (byte) (power * rearMotorCoef);
    	
    	
    	
    	
    	if(angle < 0){    		
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
    	
    	double a = TRAVEL_A, b = TRAVEL_MIN;
    	
    	double power = a*Math.abs(displacement) +b;
    	power = Math.min(TRAVEL_MAX, power);
    	
    	
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
    	
		double a = SIDEWAYS_A, minPower = SIDEWAYS_MIN;
    	double power = a*Math.abs(displacement) +minPower;   	
    	
    	power = Math.min(SIDEWAYS_MAX, power);
    	
    	
    	double arcCorrectionCoef = 0.2;
    	byte arcCorrectionPower = (byte)SIDEWAYS_ARC_POWER;
    	
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
		leftMotorPower = (byte) SIDEWAYS_ARC_POWER;//(power*arcCorrectionCoef);
    	rightMotorPower = (byte) SIDEWAYS_ARC_POWER;//(power*arcCorrectionCoef);
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
	
	
	
	
	
	public void setRCValue(String name, double value){
		
		//System.out.println("Setting "+name+"="+value);
		
		if(name.equals("ROTATE_MIN")){
        	this.ROTATE_MIN = value;
        	System.out.println(this.ROTATE_MIN);
        }
        else if(name.equals("ROTATE_MAX")){
        	this.ROTATE_MAX = value;
        }
        else if(name.equals("ROTATE_A")){
        	this.ROTATE_A = value;
        }
        else if(name.equals("ROTATE_REAR_COEF")){
        	this.ROTATE_REAR_COEF = value;
        }
        else if(name.equals("ROTATE_INITIAL_BOOST_COEF")){
        	this.ROTATE_INITIAL_BOOST_COEF = value;
        }
		
        
        
        else if(name.equals("TRAVEL_MIN")){
        	this.TRAVEL_MIN = value;
        }
        else if(name.equals("TRAVEL_MAX")){
        	this.TRAVEL_MAX = value;
        }
        else if(name.equals("TRAVEL_A")){
        	this.TRAVEL_A = value;
        }
        
        
        else if(name.equals("SIDEWAYS_MIN")){
        	this.SIDEWAYS_MIN = value;
        }
        else if(name.equals("SIDEWAYS_MAX")){
        	this.SIDEWAYS_MAX = value;
        }
        else if(name.equals("SIDEWAYS_A")){
        	this.SIDEWAYS_A = value;
        }	        
        else if(name.equals("SIDEWAYS_ARC_POWER")){
        	this.SIDEWAYS_ARC_POWER = (byte) value;
        }
	}
	
	
	public double getRCValue(String name){
		if(name.equals("ROTATE_MIN")){
        	return this.ROTATE_MIN; 
        }
        else if(name.equals("ROTATE_MAX")){
        	return this.ROTATE_MAX;
        }
        else if(name.equals("ROTATE_A")){
        	return this.ROTATE_A;
        }
        else if(name.equals("ROTATE_REAR_COEF")){
        	return this.ROTATE_REAR_COEF;
        }
        else if(name.equals("ROTATE_INITIAL_BOOST_COEF")){
        	return this.ROTATE_INITIAL_BOOST_COEF;
        }
		
        
        
        else if(name.equals("TRAVEL_MIN")){
        	return this.TRAVEL_MIN;
        }
        else if(name.equals("TRAVEL_MAX")){
        	return this.TRAVEL_MAX;
        }
        else if(name.equals("TRAVEL_A")){
        	return this.TRAVEL_A;
        }
        
        
        else if(name.equals("SIDEWAYS_MIN")){
        	return this.SIDEWAYS_MIN;
        }
        else if(name.equals("SIDEWAYS_MAX")){
        	return this.SIDEWAYS_MAX;
        }
        else if(name.equals("SIDEWAYS_A")){
        	return this.SIDEWAYS_A;
        }	        
        else if(name.equals("SIDEWAYS_ARC_POWER")){
        	return this.SIDEWAYS_ARC_POWER;
        }
        else{
        	return 0;
        }
	}
	
	
	
	public String saveConfig(File file){
		Map<String, Object> data = new HashMap<String, Object>();
	    
		
	    

		data.put("ROTATE_MIN", this.ROTATE_MIN);
		data.put("ROTATE_MAX", this.ROTATE_MIN);
		data.put("ROTATE_A", this.ROTATE_A);
		data.put("ROTATE_REAR_COEF", this.ROTATE_REAR_COEF);
		data.put("ROTATE_INITIAL_BOOST_COEF", this.ROTATE_INITIAL_BOOST_COEF);
		
		
		data.put("TRAVEL_MIN", this.TRAVEL_MIN);
		data.put("TRAVEL_MAX", this.TRAVEL_MAX);
		data.put("TRAVEL_A", this.TRAVEL_A);
        
        
		data.put("SIDEWAYS_MIN", this.SIDEWAYS_MIN);
		data.put("SIDEWAYS_MAX", this.SIDEWAYS_MAX);
		data.put("SIDEWAYS_A", this.SIDEWAYS_A);
		data.put("SIDEWAYS_ARC_POWER", this.SIDEWAYS_ARC_POWER);
		
		
		Yaml yaml = new Yaml();
	    String yamlData = yaml.dump(data);
	    //System.out.println(yamlData);
	    
	    BufferedWriter output;
		try {
			output = new BufferedWriter(new FileWriter(file));
			output.write(yamlData);
			output.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
	    return yamlData;
	    
	}
	
	
	public boolean loadConfig(File file){
		
		//System.out.println("Setting "+name+"="+value);
	
		Yaml yaml = new Yaml();
		Map<String, Object> data = null;
	    
		try {
			InputStream input = new FileInputStream(file);
			data = (Map<String, Object>) yaml.load(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(data == null)
			return false;

		
		this.ROTATE_MIN = (double) data.get("ROTATE_MIN");
		this.ROTATE_MAX = (double) data.get("ROTATE_MAX");
		this.ROTATE_A = (double) data.get("ROTATE_A");
		this.ROTATE_REAR_COEF = (double) data.get("ROTATE_REAR_COEF");
		this.ROTATE_INITIAL_BOOST_COEF = (double) data.get("ROTATE_INITIAL_BOOST_COEF");
		
		
		this.TRAVEL_MIN = (double) data.get("TRAVEL_MIN");
		this.TRAVEL_MAX = (double) data.get("TRAVEL_MAX");
		this.TRAVEL_A = (double) data.get("TRAVEL_A");
        
		this.SIDEWAYS_MIN = (double) data.get("SIDEWAYS_MIN");
		this.SIDEWAYS_MAX = (double) data.get("SIDEWAYS_MAX");
		this.SIDEWAYS_A = (double) data.get("SIDEWAYS_A");
		this.SIDEWAYS_ARC_POWER = ((Integer) data.get("SIDEWAYS_ARC_POWER")).byteValue();
		
	    return true;
	}
    
}