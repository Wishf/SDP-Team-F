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

public class TestStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	boolean isReady = false;
	
	

	public TestStrategy(BrickCommServer brick) {
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
		
		
		double dx = ballX - attackerRobotX;
		double dy = ballY - attackerRobotY;	
		double targetAngle = calcTargetAngle(dx, dy);
		double angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
		boolean ballInDefenderArea = false;
		boolean saveBall = false;
		
		/*if(Math.abs(angleDifference) < 10)
		{
			angleDifference = 0;
		}
		if (Math.abs(angleDifference) < 30){
			angleDifference = -angleDifference*0.3;
		}*/
			
		
		boolean move_robot = false;
		
		
		
		boolean ballInEnemyAttackerArea = false;
		boolean alignWithEnemyAttacker = false;
		
		
		if (ballX < defenderCheck == worldState.weAreShootingRight) {
            ballInDefenderArea = true;            
        }
        if(worldState.weAreShootingRight){        	
        	ballInEnemyAttackerArea = ballX > defenderCheck && ballX < leftCheck;
        }else{
        	ballInEnemyAttackerArea = ballX < defenderCheck && ballX > rightCheck;
        }
        //String message = String.valueOf(ballInEnemyAttackerArea);
        //RobotDebugWindow.messageDefender.setMessage(message);
        
        if(!ballCaughtDefender && ballInDefenderArea){
        	//System.out.print("I am after the ball. ");
            //target = new Point2(ballX, ballY);  
            dx = ballX - defenderRobotX;
            dy = ballY - defenderRobotY;
            saveBall = true;            
        }else {        	
            //target = new Point2(ourGoalY[1], defenderRobotX);
        	dy = ourGoalY[1] - defenderRobotY;
        }
        
        
        if(ballInEnemyAttackerArea){
        	alignWithEnemyAttacker = true;
        	//System.out.println("ALIGN");
        	//RobotDebugWindow.messageDefender.setMessage("ALIGN");
        	if(enemyAttackerRobotY - defenderRobotY < 0 ){        
        	dy = Math.max(enemyAttackerRobotY - defenderRobotY , ourGoalY[2] - defenderRobotY);        	
        	}else {
        	dy = Math.min(enemyAttackerRobotY - defenderRobotY , ourGoalY[0] - defenderRobotY);      	
        	}
        }
        double targetDistance = Math.sqrt(dx*dx + dy*dy);
        if(Math.abs(targetDistance) > 15) {
			move_robot = true;
			
			//System.out.println("Need to move the robot since dY=" + targetDistance);
		}
		boolean check = isRobotTooClose(defenderRobotX, defenderRobotY);
		RobotDebugWindow.messageAttacker.setMessage("" + check);
		synchronized (this.controlThread) {
			/*if(Math.abs(angleDifference )> 0) {
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.rotateBy = (int) angleDifference;
			} else if(move_robot) {
				////System.out.println("A: ");
				this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
				controlThread.operation.travelDistance = (int) Math.abs(targetDistance);
			}*/
			if(check){
				//RobotDebugWindow.messageAttacker.setMessage("SAVE: " + targetDistance);
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.rotateBy = (int) 120;
			}
			/*else if(alignWithEnemyAttacker){
				
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
							brick.execute(new RobotCommand.Trave(travelDist));
						}
						break;
					case DESIDEWAYS:
						if (travelDist != 0) {
							brick.execute(new RobotCommand.TravelSideways(
									travelDist));
						}
						break;
					/*case DEBACK:
						if (travelDist != 0) {
							brick.execute(new RobotCommand.Trave(
									travelDist));
						}
						break;*/
					case DEFCATCH:
						if((System.currentTimeMillis() - kickTime > 3000)){
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