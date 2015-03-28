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
    boolean stopControlThread;
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	
	
	

	public NewInterceptorStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
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

		RobotDebugWindow.messageAttacker.setMessage("inter");
		ballPositions.addLast(new Vector2f(worldState.getBall().x, worldState
				.getBall().y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

/*		double slope = (enemyAttackerRobotY - ballY) / ((enemyAttackerRobotX - ballX) + 0.0001);
		double c = ballY - slope * ballX;
		
		int targetY = (int) (slope * defenderRobotX + c);
		float dist;*/
		

		
		float targetY = ourGoalEdges[1];

			// Get equation of line through enemyAttacker along its orientation
			if(worldState.weAreShootingRight){
				if (enemyAttackerOrientation<270 && enemyAttackerOrientation >90) { 
					double rad = enemyAttackerOrientation * Math.PI/180;
					targetY = -(float) Math.tan(rad)*(enemyAttackerRobotX -defenderRobotX) + enemyAttackerRobotY;
				
				}else{
					targetY = enemyAttackerRobotY;
				}
			}
			else{
				if (enemyAttackerOrientation<90 || enemyAttackerOrientation >270) { 
					double rad = enemyAttackerOrientation * Math.PI/180;
					targetY = (float) Math.tan(rad)*(defenderRobotX-enemyAttackerRobotX) + enemyAttackerRobotY;
				}
				else{
					targetY = enemyAttackerRobotY;
				}
			}
		
		boolean faceEnemyAttacker = false;
		double angleToEnemyAttacker = 0;
		
		if (targetY > ourGoalEdges[2]) {
			targetY = (int) ourGoalEdges[2];
			//faceEnemyAttacker = true;
			//angleToEnemyAttacker = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, enemyAttackerRobotX, enemyAttackerRobotY );
		} else if (targetY < ourGoalEdges[0]) {
			targetY = (int) ourGoalEdges[0];
			//faceEnemyAttacker = true;
			//angleToEnemyAttacker = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, enemyAttackerRobotX, enemyAttackerRobotY );			
		}
		
		// Correct for defender plate not being in centre of robot
		//targetY += defenderOffset;
		
		float dist = targetY - defenderRobotY;
		
		if(!worldState.weAreShootingRight){
			dist = -dist;
		}
		// if we are shooting right and we need to move right sideways, the distance is positive so convert it to be negative.
		
		double angle = 0;

		
		boolean fixOrientation = false;
		boolean move_sideways = false;
		boolean fixRotate = false;
		double fixAngle = 0;
		
		if (Math.abs(dist) >30) {
			move_sideways = true;
		}
		//else {
			//fixOrientation = true;
			/*fixAngle = 180 - defenderOrientation;
			if(weAreShootingRight) {
				if(defenderOrientation >0 && defenderOrientation <180 )
					fixAngle  = -defenderOrientation;
				if(defenderOrientation>180 && defenderOrientation <360){
					fixAngle = 360-defenderOrientation;
				}
			}*/
		//}
		fixAngle = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderCheck, defenderRobotY);
		if (Math.abs(fixAngle)>20) {
			fixRotate = true;
		}
		System.out.println("................................................" + fixRotate);
		synchronized (this.controlThread) {
			if (fixRotate) {
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.angleDifference =  (fixAngle);
			}else if(move_sideways) {
				//System.out.println("MOVE_sideway" + dist);
/*				RobotDebugWindow.messageDefender.setMessage("MS"+dist);
				RobotDebugWindow.messageAttacker.setMessage("Aim"+targetY);*/
				
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
				while (!stopControlThread) {
					Operation.Type op;
					double rotateBy;
					double travelDist;
					synchronized (this) {
						//System.out.println("inter");
						op = this.operation.op;
						rotateBy = this.operation.angleDifference;
						travelDist = this.operation.travelDistance;
					}

					switch (op) {
					case DESIDEWAYS:
						if (travelDist != 0) {
		
							System.out.println("move!"+travelDist);
							brick.robotController.travelSideways((
                                    travelDist*0.5));
						}
						break;
					case DEFROTATE:
                        if (rotateBy != 0) {
                        	System.out.println("rotate "+rotateBy);
                            brick.robotController.rotate(rotateBy);
                        }
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


