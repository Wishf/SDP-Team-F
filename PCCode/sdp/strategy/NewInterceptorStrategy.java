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

public class NewInterceptorStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	
	
	

	public NewInterceptorStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
	}


	@Override
	public void startControlThread() {
		this.controlThread.start();
	}
	
	@Override
	public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);

		 
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

/*		double slope = (enemyAttackerRobotY - ballY) / ((enemyAttackerRobotX - ballX) + 0.0001);
		double c = ballY - slope * ballX;
		
		int targetY = (int) (slope * defenderRobotX + c);
		float dist;*/
		
		boolean ballInEnemyAttackerArea = false;
		boolean alignWithEnemyAttacker = false;
		
		float targetY = ourGoalEdges[1];
		
		if (worldState.ballNotOnPitch) {
			alignWithEnemyAttacker = true;
			// Get equation of line through enemyAttacker along its orientation
			//RobotDebugWindow.messageDefender.setMessage("Enemy_angle:"+enemyAttackerOrientation+" Aim at:");
			if (enemyAttackerOrientation<90 || enemyAttackerOrientation >270) { 
				double rad = enemyAttackerOrientation * Math.PI/180;
				targetY = (float) Math.tan(rad)*(defenderRobotX-enemyAttackerRobotX) + defenderRobotY;
				//RobotDebugWindow.messageAttacker.setMessage(""+targetY);
			}else{
				targetY = ourGoalY[1];
			}
		}
		if (targetY > ourGoalEdges[2]) {
			targetY = (int) ourGoalEdges[2];
		} else if (targetY < ourGoalEdges[0]) {
			targetY = (int) ourGoalEdges[0];
		}
		
		// Correct for defender plate not being in centre of robot
		//targetY += defenderOffset;
		
		float dist = targetY - defenderRobotY;
		System.out.println("Enemy_angle:"+enemyAttackerOrientation+" Aim at:"+targetY+" Defender needs move:"+dist);
		
		boolean move_sideways = false;
		
		if (dist >10) {
			move_sideways = true;
		}

		
		synchronized (this.controlThread) {
			if(move_sideways){
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = (int) dist;
			}
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
					/*case DEFROTATE:
						if (rotateBy != 0) {
						brick.executeSync(new RobotCommand.Rotate(
								rotateBy));
						}
						break;
					case DEFTRAVEL:
						 if (travelDist != 0) {
							brick.execute(new RobotCommand.Trave(travelDist));
						}
						break;*/
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
/*					case DEFCATCH:
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
						break;*/
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