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

public class CornerStrategy extends GeneralStrategy {


    private BrickCommServer brick;
    private ControlThread controlThread;
    private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
    private boolean kicked;
    private boolean catcherReleased;
    

    public CornerStrategy(BrickCommServer brick) {
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
        //ControlBox.controlBox.reset();
       
        MovingObject ball = worldState.getBall();

        boolean ballInDefenderArea = false;
        double angleToDefCheck = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderCheck, defenderRobotY);
        
        
       

        boolean rotate = false;
        
        double targetAngle = 0;
        double angleDifference = 0;
        double dx = 0;
        double dy = 0;
        double distanceToDefault = ourGoalY[1] - defenderRobotY;
        boolean move_sideways = false;       
        double catchThreshold = 35;
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
        boolean move_robot = false;
        
		double angleToTeamMate = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attackerRobotX, attackerRobotY);
		
		 
        
             
        
      
        
        double targetDistance;
        double ballDistance = Math.sqrt((ballX - defenderRobotX)*(ballX - defenderRobotX) + (ballY - defenderRobotY)*(ballY - defenderRobotY));
               	
        boolean tooClose = isRobotTooClose(defenderRobotX, defenderRobotY, 30);	
        System.out.println(tooClose);
    
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
        if(ballInDefenderArea && distanceToDefault > 20 ){
        	move_sideways = true;
        }
        
        

        synchronized (this.controlThread) {
            //this.controlThread.operation.op = Operation.Type.DO_NOTHING;
        	if (tooClose) {
                System.out.println("ROBOT TOO CLOSE");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
                
            } 
            
            else{
            	this.controlThread.operation.op = Operation.Type.DO_NOTHING;
            	System.out.println("DO NOTHING");
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
                while (true) {
                    Operation.Type op;
                    double rotateBy;
					double travelDist;
                    
                	if(!start){
                		start = true;
                		op = Operation.Type.DEFUNCATCH;
                		rotateBy = 0;
                		travelDist = 0;
                	}
                	else {
	                    synchronized (this) {
	                        op = this.operation.op;
	                        rotateBy = this.operation.angleDifference;
	                        travelDist = this.operation.travelDistance;
	                    }
                	}
                    //System.out.println("operation: " + op);
                    switch (op) {
                        case DEFROTATE:
                        	if(System.currentTimeMillis() - caughtTime < 1500)
                        	{
                        		brick.execute(new RobotCommand.Stop());
                        	}
                            if (rotateBy != 0) {
                                brick.executeSync(new RobotCommand.Rotate(
                                        rotateBy));
                            }
                            break;
                        case DEFTRAVEL:
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.execute(new RobotCommand.Stop());
                        	}
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Travel(
                                        travelDist));
                            }
                            break;
                        case DESIDEWAYS:
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.execute(new RobotCommand.Stop());
                        	}
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.TravelSideways(
                                        travelDist));
                            }
                            break;
                        case DEBACK:
                        	if(System.currentTimeMillis() - caughtTime < 1000)
                        	{
                        		brick.execute(new RobotCommand.Stop());
                        	}
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Travel(
                                        travelDist));
                            }
                            break;
                        case DEFCATCH:
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //System.out.println("Catching");


                                brick.execute(new RobotCommand.ResetCatcher());
                                ballCaughtDefender = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                        	if(System.currentTimeMillis() - caughtTime < 2000)
                        	{
                        		brick.execute(new RobotCommand.Stop());
                        	}
                            if((System.currentTimeMillis() - caughtTime > 2000)){
                                //System.out.println("Kicking");

                                brick.execute(new RobotCommand.Kick());
                                Thread.sleep(500);
                                brick.execute(new RobotCommand.Catch());

                                kicked = true;
                                ballCaughtDefender = false;
                                kickTime = System.currentTimeMillis();
                            }
                            break;
                        case DEFUNCATCH:
                        	
                            brick.execute(new RobotCommand.Catch());
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
