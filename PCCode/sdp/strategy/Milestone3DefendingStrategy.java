package sdp.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.vision.gui.tools.RobotDebugWindow;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.strategy.ControlBox;
import sdp.strategy.Operation.Type;


public class Milestone3DefendingStrategy extends GeneralStrategy {


    private BrickCommServer brick;
    private ControlThread controlThread;
    private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
    private boolean kicked;
    private boolean catcherReleased;
    boolean stopControlThread;
    

    public Milestone3DefendingStrategy(BrickCommServer brick) {
        this.brick = brick;
        this.controlThread = new ControlThread();
        //System.out.println("Starting.");
    }
    @Override
	public void stopControlThread() {
		stopControlThread = true;
	}

	@Override
	public void startControlThread() {
		stopControlThread = false;
		controlThread.start();
	}

    @Override
    public void sendWorldState(WorldState worldState) {
        super.sendWorldState(worldState);
        brick.robotController.setWorldState(worldState);
        //ControlBox.controlBox.reset();
        
        
        MovingObject ball = worldState.getBall();

        boolean ballInDefenderArea = false;
        double angleToDefCheck = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderCheck, defenderRobotY);
        
        
        ballPositions.addLast(new Vector2f(ball.x, ball.y));
        if (ballPositions.size() > 3)
            ballPositions.removeFirst();

        if (ballX < defenderCheck == worldState.weAreShootingRight) {
            ballInDefenderArea = true;
            //System.out.println("BALL");
        }

        boolean rotate = false;
        
       
        double angleDifference = 0;
        double dx = 0;
        double dy = 0;
        double distanceToDefault = ourGoalY[1] - defenderRobotY;
        boolean move_sideways = false;
        boolean robotTooClose = isObjectTooClose(defenderRobotX, defenderRobotY, 40);
        boolean moveAway = false;
        double angleToCentre = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderRobotX, ourGoalY[1]);
       
        if(robotTooClose){
    		moveAway = true;
    		
    	}
       
        
        if(!ballCaughtDefender && ballInDefenderArea){        	
            dx = ballX - defenderRobotX;
            dy = ballY - defenderRobotY;
            angleDifference = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, ballX, ballY);
            
        }else if (!ballCaughtDefender && Math.abs(angleToDefCheck) > 15 && !ballInDefenderArea) {         	
        	rotate = true;
        	angleDifference = angleToDefCheck;     
        	//System.out.println("ROTATE TO DEFCHECK");
        }       

       
        if(angleDifference < 0) {
            angleDifference += 360;
        }

        if(angleDifference > 180) {
            angleDifference -= 360;
        }

        
        if(Math.abs(angleDifference) > 15.0 ) {        	
            rotate = true;
            //System.out.println("ROTATE TO BALL");
        }

       
        double catchThreshold = 30;
       
        
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
        boolean move_robot = false;
        boolean move_back = false;
        
		double angleToTeamMate = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attackerRobotX, attackerRobotY);        
        double ballDistance = Math.sqrt((ballX - defenderRobotX)*(ballX - defenderRobotX) + (ballY - defenderRobotY)*(ballY - defenderRobotY));
        
        //RobotDebugWindow.messageAttacker.setMessage("" + ballCaughtDefender);
        double targetDistance;
        //BAL CAUGHT
        if(ballCaughtDefender){
        	
        	//ball has slipped away
        	if(!worldState.ballNotOnPitch && ballDistance > 2*catchThreshold) {   
            	//System.out.println(" ................................");
            	ballCaughtDefender = false;
            	catcherReleased = false;
            	uncatch = true;            	
            } 
        	
        	ControlBox.controlBox.computePositions(worldState);
            Point2 our_goal = ControlBox.controlBox.getDefenderPosition();
        	dy = our_goal.getY() - defenderRobotY;
        	dx = our_goal.getX() - defenderRobotX;
        	targetDistance = Math.sqrt(dx*dx + dy*dy);
        	angleDifference = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, our_goal.getX(), our_goal.getY());
        	//RobotDebugWindow.messageDefender.setMessage("Target" + our_goal.getX() + "  " + our_goal.getY());
        	
        	
        	       	
        	if(Math.abs(angleToTeamMate) < 15 && Math.abs(targetDistance )< 25){
        		//System.out.println(".................TRUE  "  + angleToTeamMate);
            
	        	if(!catcherReleased){	        		
	        		uncatch = true;	        		
	        	}
	        	else{
		            kick_ball = true;
		            
	        	}
        	}        	
        	else if(Math.abs(targetDistance) < 25 && Math.abs(angleToTeamMate) > 15 ){
        		rotate = true;
        		angleDifference = angleToTeamMate; 
        		//System.out.println("ROTATE TO TEAMMATE");
        		
        	}
        	else if(targetDistance > 25 && Math.abs(angleDifference)>15  ){
        		
        		angleDifference = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation,our_goal.getX(), our_goal.getY());
        		rotate = true;
        		//System.out.println("ROTATE TO TARGET");
        		
        	}
        	
        } 
        targetDistance = Math.sqrt(dx*dx + dy*dy);
        
        if((ballDistance < catchThreshold && !ballCaughtDefender)  /*&& ballInDefenderArea*/) {
            catch_ball = true;
            
        }

        if(targetDistance > 35) {
            move_robot = true;           
        }
        
        if(!ballInDefenderArea && Math.abs(angleToDefCheck) < 15 && !ballCaughtDefender){
        	rotate = false;
        	//System.out.println("NO NEED TO ROTATE");
        }
        if(!ballInDefenderArea && Math.abs(distanceToDefault) > 25 ){
        	move_sideways = true;
        	
        }
        if(Math.abs(defenderCheck - defenderRobotX ) < 15){
        	move_back = true;
        	//System.out.println("MOVE BACK");
        }
        
        

        synchronized (this.controlThread) {
        	
           
        	if (uncatch && !catcherReleased) {
                
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
                catcherReleased = true;
                
            } else if(rotate){            	
            	 
            	 if(isObjectTooClose(defenderRobotX, defenderRobotY, 30)){
            		 
            			 //System.out.println("TRUE");
            			 this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
            			 if(Math.abs(angleToCentre) <80){
	            			 controlThread.operation.travelDistance = 40;
	            		 }else{
	            			 controlThread.operation.travelDistance = - 40;
	            		 }
            		 
            	 }else{
            		 //System.out.println("FALSE");
            		 this.controlThread.operation.op = Operation.Type.DEFROTATE;
            		 controlThread.operation.angleDifference = angleDifference;
            	 }
            	 
            }
        	
            else if (catch_ball) {
            	 if(isObjectTooClose(defenderRobotX, defenderRobotY, 25)){
            		 
            		
            			 //System.out.println("TRUE");
           			 this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
            		 if(Math.abs(angleToCentre) <80){
	            		 controlThread.operation.travelDistance = 40;
            		 }else{
            			 controlThread.operation.travelDistance = - 40;
	            		 
            		 }
            	 }else{
            		 //System.out.println("CATCH");
	                this.controlThread.operation.op = Operation.Type.DEFCATCH;                
	                catcherReleased = false;      
            	 }
            } 
        	
            else if (kick_ball) {            	
                this.controlThread.operation.op = Operation.Type.DEFKICK;                
                
            }
        	
            else if (move_robot) {
            	 if(isObjectTooClose(defenderRobotX, defenderRobotY,20)){
            		 
            			 //System.out.println("TRUE");
	            		 this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
	            		 if(Math.abs(angleToCentre) <100){
	            			 controlThread.operation.travelDistance = 40;
	            		 }else{
	            			 System.out.println();
	            			 controlThread.operation.travelDistance = - 40;
	            		 }
            		 
            	 }else{
	                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
	                controlThread.operation.travelDistance = (int) targetDistance;  
            	 }
               
            }
        	
            else if(move_sideways){
            	this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
            	//System.out.println("DEFAULT" + distanceToDefault);
            	if(weAreShootingRight)
            		controlThread.operation.travelDistance = distanceToDefault; 
            	else
            		controlThread.operation.travelDistance = -distanceToDefault;
            }
            else if(move_back){
            	this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = -25; 
            }
            /*else{
            	this.controlThread.operation.op = Operation.Type.DO_NOTHING;       
            	RobotDebugWindow.messageDefender.setMessage("NOTHING");
            }*/
        }

    }

    protected class ControlThread extends Thread {
        public Operation operation = new Operation();
        private ControlThread controlThread;
        private long kickTime;
        private long caughtTime;
        private boolean start = false;

        public ControlThread() {
            super("Robot control thread");
            setDaemon(true);
        }
        @Override


        public void run() {
        	
        	try {
                while (!stopControlThread) {
                    Operation.Type op;
                    double rotateBy;
					double travelDist;
                    
                
	                 synchronized (this) {
	                    	//System.out.println("our zone");
	                	// RobotDebugWindow.messageDefender.setMessage("MS"); 
	                	 op = this.operation.op;
	                        rotateBy = this.operation.angleDifference;
	                        travelDist = this.operation.travelDistance;
	                    }
                    //System.out.println("operation: " + op);
                    switch (op) {
                        case DEFROTATE:
                        	RobotDebugWindow.messageAttacker.setMessage("rotate");
                        	if(System.currentTimeMillis() - caughtTime < 1500)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (rotateBy != 0) {
                                brick.robotController.rotate(rotateBy);
                            }
                            break;
                        case DEFTRAVEL:
                        	RobotDebugWindow.messageAttacker.setMessage("MOVE");
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (travelDist != 0) {
                                brick.robotController.travel(travelDist);
                            }
                            break;
                        case DESIDEWAYS:
                        	RobotDebugWindow.messageAttacker.setMessage("SIDEWAYS");
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (travelDist != 0) {
                                brick.robotController.travelSideways(travelDist);
                            }
                            break;
                        case DEBACK:
                        	RobotDebugWindow.messageAttacker.setMessage("BACKWARDS");
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (travelDist != 0) {
                                brick.robotController.travel(travelDist);
                            }
                            break;
                        case DEFCATCH:
                        	RobotDebugWindow.messageAttacker.setMessage("CATCH");
                            if((System.currentTimeMillis() - kickTime > 1000)){
                                System.out.println("CATCH");
                                brick.robotController.closeCatcher();
                                ballCaughtDefender = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                        	RobotDebugWindow.messageAttacker.setMessage("KICK");
                        	if(System.currentTimeMillis() - caughtTime < 2000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if((System.currentTimeMillis() - caughtTime > 2000)){
                                //System.out.println("Kicking");

                                brick.robotController.kick();
                                //Thread.sleep(500);
                                //brick.robotController.openCatcher();

                                kicked = true;
                                ballCaughtDefender = false;
                                kickTime = System.currentTimeMillis();
                            }
                            break;
                        case DEFUNCATCH:
                        	
                        	brick.robotController.openCatcher();
                            break;
                        default:
                            break;
                    }
                    Thread.sleep(StrategyController.STRATEGY_TICK);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            finally {}

        }
    }

}


