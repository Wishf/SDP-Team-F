package sdp.strategy;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.vision.gui.tools.RobotDebugWindow;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;


/**
 * @author s1250014
 *
 *Prints the catcher and ball states in the debug windwow
 */
public class TestCatcherStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	boolean isReady = false;
	
	

	public TestCatcherStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
	}

	@Override
	public void stopControlThread() {
		this.controlThread.stop();
	}

	@Override
	public void startControlThread() {
		this.controlThread.start();
	}
	public boolean isRobotReady(){
		return isReady;
	}
	
	@Override
	public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);
		brick.robotController.setWorldState(worldState);
		
		
		double ballDistance = Math.sqrt((ballX - defenderRobotX)*(ballX - defenderRobotX) + (ballY - defenderRobotY)*(ballY - defenderRobotY));
		
		double catchThreshold = 35;
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean rotate = false;
        boolean move_robot = false;
        double dx = 0;
		double dy = 0;	
		double angleDifference = 0;
		
		
		String message = "CATCHER: "+brick.robotController.getCatcherState().name()+"; ";
		message += "BALL CAUGHT: "+brick.robotController.ballCaught+";";
		
		RobotDebugWindow.messageAttacker.setMessage(message);
        
        //System.out.println(message);
		
		
        /*double targetAngle;//calcTargetAngle(dx, dy);
		
		
		if(worldState.weAreShootingRight){
			targetAngle = 0;
		}
		else{
			targetAngle = 180;
		}
			
		if(this.brick.name.equals("attacker")){
			dx = ballX - attackerRobotX;
			dy = ballY - attackerRobotY;
			angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
		}
		else{
			dx = ballX - defenderRobotX;
			dy = ballY - defenderRobotY;
			angleDifference = calcAngleDiff(defenderOrientation, targetAngle);
		}
        
		
		if(Math.abs(angleDifference) > 20)
		{
			rotate = true;
		}
		
		if(Math.abs(dy) > 20){
			move_robot = true;
		}
		
        if(ballCaught){
        	kick_ball = true;
        }
        else if((ballDistance < catchThreshold)  && ballInDefenderArea) {
            catch_ball = true;            
        }
		*/
		
		
		
		
		
		synchronized (this.controlThread) {
			
			
			
			if (catch_ball) {
                //System.out.println("CATCH");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
			}
			 else if (kick_ball) {
		        RobotDebugWindow.messageAttacker.setMessage("Kick");
			    this.controlThread.operation.op = Operation.Type.DEFKICK;
			} 
			 else if(rotate){
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.angleDifference = angleDifference;
			}
			else if(move_robot){
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = dy;
			}
			else{
				this.controlThread.operation.op = Operation.Type.STOP;				
			}
			
			
			/*
			 * if(Math.abs(angleDifference )> 0) {
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.rotateBy = (int) angleDifference;
			} else if(move_robot) {
				////System.out.println("A: ");
				this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
				controlThread.operation.travelDistance = (int) Math.abs(targetDistance);
			}
			else if(true){
				//RobotDebugWindow.messageAttacker.setMessage("SAVE: " + targetDistance);
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = (int) 220;
			}
			else if(alignWithEnemyAttacker){
				
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = (int) targetDistance;
			}*/
			
			
		}
	}
	
	protected class ControlThread extends Thread {
		public Operation operation = new Operation();
		private ControlThread controlThread;

		public ControlThread() {
			super("Robot control thread");
			setDaemon(true);
		}
		@Override
		
		
		public void run() {
			try {
				while (true) {
					Operation.Type op;
					double rotateBy, travelDist;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.angleDifference;
						travelDist = this.operation.travelDistance;
					}
					//System.out.println("operation: " + op);
					switch (op) {
					case STOP:
						//brick.robotController.stop();
						//RobotDebugWindow.messageAttacker.setMessage("STOP");
						break;
					case DEFROTATE:
						brick.robotController.rotate(-rotateBy);
						//RobotDebugWindow.messageAttacker.setMessage("Rotating: "+angleDifference);
						break;
					case DEFTRAVEL:
						 brick.robotController.travel(travelDist);
						break;
					case DESIDEWAYS:
						brick.robotController.travelSideways(-travelDist);
						break;
					/*case DEBACK:
						if (travelDist != 0) {
							brick.execute(new RobotCommand.Trave(
									travelDist));
						}
						break;*/
					case DEFCATCH:
						if((System.currentTimeMillis() - kickTime > 500)){
							brick.execute(new RobotCommand.Catch());
							ballCaught = true;
							caughtTime = System.currentTimeMillis();
							kicked = false;
						}
						break;
					case DEFKICK:
						if((System.currentTimeMillis() - caughtTime > 1000)){
							brick.execute(new RobotCommand.Kick());
							
							kicked = true;
							ballCaught = false;
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