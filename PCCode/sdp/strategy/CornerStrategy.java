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
       
        double angleToTop = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderRobotX, topY);
        double angleToBottom = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderRobotX, topY);
        
        
        
       
        
		
		
		 
        
             
        
      
        
        
               	
        boolean tooClose = isRobotTooClose(defenderRobotX, defenderRobotY, 30);	
        System.out.println(tooClose);
    
       
        
        

       
        
       

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
