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

public class Milestone3DefendingStrategy extends GeneralStrategy {


    private BrickCommServer brick;
    private ControlThread controlThread;
    private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
    private boolean kicked;

    public Milestone3DefendingStrategy(BrickCommServer brick) {
        this.brick = brick;
        this.controlThread = new ControlThread();
        //System.out.println("Starting.");
    }

    @Override
    public void stopControlThread() {
        this.controlThread.stop();
    }

    @Override
    public void startControlThread() {
        this.controlThread.start();
    }

    @Override
    public void sendWorldState(WorldState worldState) {
        super.sendWorldState(worldState);
        
        //ControlBox.controlBox.computePositions(worldState);
        //Point2 our_goal = ControlBox.controlBox.getDefenderPosition();
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
        double targetAngle;
        double dx = 0;
        double dy = 0;

        Vector2f ball3FramesAgo = ballPositions.getFirst();
        float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
        float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;
        ControlBox.controlBox.computePositions(worldState);
        //System.out.println("ROBOT: " + worldState.getEnemyAttackerRobot().y);

        
        if(!ballCaughtDefender && ballInDefenderArea){
        	//System.out.print("I am after the ball. ");
            //target = new Point2(ballX, ballY);  
            dx = ballX - defenderRobotX;
            dy = ballY - defenderRobotY;
        }else {        	
            //target = new Point2(ourGoalY[1], defenderRobotX);
        	dy = ourGoalY[1] - defenderRobotY;
        }
        boolean tooClose = Math.abs(defenderRobotY - botOfPitch) < 30 || Math.abs(topOfPitch - defenderRobotY) < 30
        		|| Math.abs(ourGoalX - defenderRobotX) < 30;
        
        

        targetAngle = Math.toDegrees(Math.atan2(dy, dx)) % 360;

        
        if(targetAngle < 0){       	
            targetAngle += 360;
        }

        double angleDifference = (targetAngle - defenderOrientation) % 360;

       
        if(angleDifference < 0) {
            angleDifference += 360;
        }

        if(angleDifference > 180) {
            angleDifference -= 360;
        }

        
        if(Math.abs(angleDifference) > 20.0 ) {        	
            rotate = true;
        }

       
        double catchThreshold = 35;
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
        boolean move_robot = false;
        //boolean ballInEnemyAttackerArea = false;
		//boolean alignWithEnemyAttacker = false;
		double angleToTeamMate = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attackerRobotX, attackerRobotY);
		
		
        /*if(worldState.weAreShootingRight){        	
        	ballInEnemyAttackerArea = ballX > defenderCheck && ballX < leftCheck;
        }else{
        	ballInEnemyAttackerArea = ballX < defenderCheck && ballX > rightCheck;
        }*/
             
        
        if(!ballInDefenderArea){
        	//alignWithEnemyAttacker = true;
        	if(Math.abs(angleToDefCheck) > 15){
        		rotate = true;
        		angleDifference = angleToDefCheck;
        	}
        	else if(enemyAttackerRobotY - defenderRobotY < 0 ){        
        	dy = Math.max(enemyAttackerRobotY - defenderRobotY , ourGoalY[0] - defenderRobotY);        	
        	}else {
        	dy = Math.min(enemyAttackerRobotY - defenderRobotY , ourGoalY[2] - defenderRobotY);      	
        	}
        }
        double targetDistance = Math.sqrt(dx*dx + dy*dy);
        
      
        
        if(targetDistance < catchThreshold && !ballCaughtDefender) {
            catch_ball = true;            
        }
        
        // If the ball slips from the catching area we can guess we did not catch it.
        if(ballCaughtDefender){
        	if(targetDistance > 5*catchThreshold) {       
        	////System.out.println("I've lost the ball!");
        	ballCaughtDefender = false;
        	}
        	else if(angleToTeamMate < 25){
            // Here: need to check if the defender is ready and we don't need to move any further
            kick_ball = true;       
        	}
        	else if(angleToTeamMate > 25){
        		rotate = true;
        		angleDifference = angleToTeamMate;
        	}
        }            

        if(  (targetDistance > 25)) {
            move_robot = true;          
        }
        
        

        /*
        double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
        double c = ballY1 - slope * ballX1;
        boolean ballMovement = Math.abs(ballX2 - ballX1) < 10;
        //int targetY = (int) (slope * defenderRobotX + c);
        //double ang1 = calculateAngle(defenderRobotX, defenderRobotY,
        //		defenderOrientation, defenderRobotX, defenderRobotY - 50);
        //ang1 = ang1 / 3;

*/
        

        synchronized (this.controlThread) {
            //this.controlThread.operation.op = Operation.Type.DO_NOTHING;
            if(rotate){
            	 this.controlThread.operation.op = Operation.Type.DEFROTATE;
                 controlThread.operation.rotateBy = (int) (angleDifference);
                 RobotDebugWindow.messageDefender.setMessage("Rotate: " + angleDifference);
            }
            else if (catch_ball) {
                //System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
                RobotDebugWindow.messageDefender.setMessage("Catch Ball");
            } else if (kick_ball) {
                //System.out.println("Kick");
                this.controlThread.operation.op = Operation.Type.DEFKICK;
                RobotDebugWindow.messageDefender.setMessage("KICK!");
                
            } else if (catch_ball) {
                System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } /*else if (alignWithEnemyAttacker) {            	
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance;
                RobotDebugWindow.messageAttacker.setMessage("" + targetDistance);
                RobotDebugWindow.messageDefender.setMessage("ALIGN");
            } */else if (uncatch) {
                //System.out.println("Uncatch");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
                RobotDebugWindow.messageDefender.setMessage("UNCATCH");
            } else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance;
                RobotDebugWindow.messageDefender.setMessage("MOVE: " + targetDistance);
            }
        }

    }

    protected class ControlThread extends Thread {
        public Operation operation = new Operation();
        private ControlThread controlThread;
        private long kickTime;
        private long caughtTime;

        public ControlThread() {
            super("Robot control thread");
            setDaemon(true);
        }
        @Override


        public void run() {
        	
            try {
                while (true) {
                    Operation.Type op;
                    int rotateBy, travelDist;
                    
                    synchronized (this) {
                        op = this.operation.op;
                        rotateBy = this.operation.rotateBy;
                        travelDist = this.operation.travelDistance;
                    }
                    //System.out.println("operation: " + op);
                    switch (op) {
                        case DEFROTATE:
                            if (rotateBy != 0) {
                                brick.executeSync(new RobotCommand.Rotate(
                                        rotateBy));
                            }
                            break;
                        case DEFTRAVEL:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Trave(
                                        travelDist));
                            }
                            break;
                        case DESIDEWAYS:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.TravelSideways(
                                        travelDist));
                            }
                            break;
                        case DEBACK:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Trave(
                                        travelDist));
                            }
                        case DEFCATCH:
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //System.out.println("Catching");


                                brick.execute(new RobotCommand.Catch());
                                ballCaughtDefender = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                            if((System.currentTimeMillis() - caughtTime > 1000)){
                                //System.out.println("Kicking");

                                brick.execute(new RobotCommand.Kick());
                                Thread.sleep(500);
                                brick.execute(new RobotCommand.ResetCatcher());

                                kicked = true;
                                ballCaughtDefender = false;
                                kickTime = System.currentTimeMillis();
                            }
                            break;
                        case DEFUNCATCH:
                            brick.execute(new RobotCommand.ResetCatcher());
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
