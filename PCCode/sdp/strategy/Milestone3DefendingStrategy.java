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
        }

        boolean rotate = false;
        
        double targetAngle = 0;
        double angleDifference = 0;
        double dx = 0;
        double dy = 0;
        double distanceToDefault = ourGoalY[1] - defenderRobotY;
        boolean move_sideways = false;

        Vector2f ball3FramesAgo = ballPositions.getFirst();
        float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
        float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;
        //ControlBox.controlBox.computePositions(worldState);
        //System.out.println("ROBOT: " + worldState.getEnemyAttackerRobot().y);

        
        if(!ballCaughtDefender && ballInDefenderArea){
        	//System.out.print("I am after the ball. ");
            //target = new Point2(ballX, ballY);  
            dx = ballX - defenderRobotX;
            dy = ballY - defenderRobotY;
            angleDifference = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, ballX, ballY);
        }else if (!ballCaughtDefender && Math.abs(angleToDefCheck) > 15 && !ballInDefenderArea) {        	
            
        	System.out.println("rotateToDefCheck" + angleToDefCheck);        	
        	rotate = true;
        	angleDifference = angleToDefCheck;
        	
        }
       
        
       
        //targetAngle = Math.toDegrees(Math.atan2(dy, dx)) % 360;

        
        if(targetAngle < 0){       	
            targetAngle += 360;
        }

        //angleDifference = (targetAngle - defenderOrientation) % 360;

       
        if(angleDifference < 0) {
            angleDifference += 360;
        }

        if(angleDifference > 180) {
            angleDifference -= 360;
        }

        
        if(Math.abs(angleDifference) > 15.0 ) {        	
            rotate = true;
        }

       
        double catchThreshold = 35;
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
        boolean move_robot = false;
        
		double angleToTeamMate = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attackerRobotX, attackerRobotY);
		
		 
        /*if(worldState.weAreShootingRight){        	
        	ballInEnemyAttackerArea = ballX > defenderCheck && ballX < leftCheck;
        }else{
        	ballInEnemyAttackerArea = ballX < defenderCheck && ballX > rightCheck;
        }*/
             
        
      
        
        double targetDistance;
        double ballDistance = Math.sqrt((ballX - defenderRobotX)*(ballX - defenderRobotX) + (ballY - defenderRobotY)*(ballY - defenderRobotY));
        // If the ball slips from the catching area we can guess we did not catch it.
        if(ballCaughtDefender /*&& !(ballX < defenderCheck)*/){
        	if(!worldState.ballNotOnPitch && ballDistance > 1.2*catchThreshold) {       
            	//System.out.println("I've lost the ball!");
            	ballCaughtDefender = false;
            	uncatch = true;
            	catcherReleased = true;
            } 
        	//System.out.println("BAL.........");
        	ControlBox.controlBox.computePositions(worldState);
            Point2 our_goal = ControlBox.controlBox.getDefenderPosition();
        	dy = our_goal.getY() - defenderRobotY;
        	dx = our_goal.getX() - defenderRobotX;
        	targetDistance = Math.sqrt(dx*dx + dy*dy);
        	angleDifference = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, our_goal.getX(), our_goal.getY());
        	RobotDebugWindow.messageDefender.setMessage("Target" + our_goal.getX() + "  " + our_goal.getY());
        	System.out.println("TARGET" + targetDistance);
        	       	
        	if(Math.abs(angleToTeamMate) < 25 && Math.abs(targetDistance )< 20){
        		System.out.println("TRUE");
            // Here: need to check if the defender is ready and we don't need to move any further
	        	if(!catcherReleased){
	        		//uncatch = true;
	        		catcherReleased = true;
	        	}
	        	else{
		            kick_ball = true;
		            //System.out.println("1111111111111");
	        	}
        	}        	
        	else if(targetDistance < 25 && Math.abs(angleToTeamMate) > 15 ){
        		rotate = true;
        		angleDifference = angleToTeamMate;        		
        		System.out.println("2222222222222222222222");
        	}
        	else if(targetDistance > 25 && Math.abs(angleDifference)>15  ){
        		System.out.println("ROTATE TO TARGET POSITION" + angleDifference);
        		angleDifference = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation,our_goal.getX(), our_goal.getY());
        		rotate = true;
        		
        	}
        	
        } 
        targetDistance = Math.sqrt(dx*dx + dy*dy);
        
        if((ballDistance < catchThreshold && !ballCaughtDefender)  /*&& ballInDefenderArea*/) {
            catch_ball = true;
            catcherReleased = false;
        }

        if(targetDistance > 20) {
            move_robot = true;           
        }
        
        if(!ballInDefenderArea && Math.abs(angleToDefCheck) < 15 && !ballCaughtDefender){
        	rotate = false;
        	//System.out.println("NO NEED TO ROTATE");
        }
        if(!ballInDefenderArea && distanceToDefault > 35 ){
        	move_sideways = true;
        	
        }
        
        

        synchronized (this.controlThread) {
            //this.controlThread.operation.op = Operation.Type.DO_NOTHING;
        	if (uncatch) {
                //System.out.println("Uncatch");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
                
            } else if(rotate){
            	 this.controlThread.operation.op = Operation.Type.DEFROTATE;
            	 System.out.println("Rotate" + angleDifference);
            	 if(Math.abs(angleDifference) > 60){
            		 controlThread.operation.angleDifference = angleDifference*0.3;
            	 }else if(Math.abs(angleDifference)>30){
            		 controlThread.operation.angleDifference = angleDifference*0.5;

            	 }else{
            		 controlThread.operation.angleDifference = angleDifference*0.9;
            	 }
            	 
                
            }
            else if (catch_ball) {
                System.out.println("CATCH");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
               // RobotDebugWindow.messageAttacker.setMessage("DEF");
                //RobotDebugWindow.messageDefender.setMessage("Catch Ball");
            } else if (kick_ball) {
                //System.out.println("Kick");
                this.controlThread.operation.op = Operation.Type.DEFKICK;
                //RobotDebugWindow.messageAttacker.setMessage("DEF");
                //RobotDebugWindow.messageDefender.setMessage("KICK!");
                
            }  else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance*0.8;
                //System.out.println(" MOVE ");
                //RobotDebugWindow.messageAttacker.setMessage("DEF");
                //RobotDebugWindow.messageDefender.setMessage("MOVE: " + targetDistance);
            }else if(move_sideways){
            	this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
            	controlThread.operation.travelDistance = distanceToDefault;
            	System.out.println("DEFAULT");
            }
            else{
            	this.controlThread.operation.op = Operation.Type.DO_NOTHING;
            	//System.out.println("IDLE");
            }
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
                        	if(System.currentTimeMillis() - caughtTime < 1500)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (rotateBy != 0) {
                                brick.robotController.rotate(rotateBy);
                            }
                            break;
                        case DEFTRAVEL:
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (travelDist != 0) {
                                brick.robotController.travel(travelDist);
                            }
                            break;
                        case DESIDEWAYS:
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (travelDist != 0) {
                                brick.robotController.travelSideways(travelDist);
                            }
                            break;
                        case DEBACK:
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if (travelDist != 0) {
                                brick.robotController.travel(travelDist);
                            }
                            break;
                        case DEFCATCH:
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //System.out.println("Catching");


                                brick.robotController.closeCatcher();
                                ballCaughtDefender = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                        	if(System.currentTimeMillis() - caughtTime < 2000)
                        	{
                        		brick.robotController.stop();
                        	}
                            if((System.currentTimeMillis() - caughtTime > 2000)){
                                //System.out.println("Kicking");

                                brick.robotController.kick();
                                Thread.sleep(500);
                                brick.robotController.openCatcher();

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


