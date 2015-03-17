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
		boolean defenderNeedFixOrientation = false;
		boolean rotate = false;
		
		float targetY = ourGoalEdges[1];
		double angleToDefCheck = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, defenderCheck, defenderRobotY);
		// convert true to worldState.ballNotOnPitch if playing games
		//System.out.println("Angle: " + angleToDefCheck);
		if(Math.abs(angleToDefCheck) > 15){
			rotate = true;
			
		}
		alignWithEnemyAttacker = false;
			// Get equation of line through enemyAttacker along its orientation
			if(worldState.weAreShootingRight){
				if (enemyAttackerOrientation<270 && enemyAttackerOrientation >90) { 
					alignWithEnemyAttacker = true;
					double rad = enemyAttackerOrientation * Math.PI/180;
					targetY = -(float) Math.tan(rad)*(enemyAttackerRobotX -defenderRobotX) + enemyAttackerRobotY;
				
				}else{
					targetY = worldState.leftGoal[1];
				}
			}
			else{
				if (enemyAttackerOrientation<90 || enemyAttackerOrientation >270) { 
					double rad = enemyAttackerOrientation * Math.PI/180;
					alignWithEnemyAttacker = true;
					targetY = (float) Math.tan(rad)*(defenderRobotX-enemyAttackerRobotX) + enemyAttackerRobotY;
				}
				else{
					targetY = worldState.leftGoal[1];
				}
			}
		
		boolean faceEnemyAttacker = false;
		double angleToEnemyAttacker = 0;
		
		if (targetY > 260) {
			targetY = (int) 260;
			faceEnemyAttacker = true;
			angleToEnemyAttacker = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, enemyAttackerRobotX, enemyAttackerRobotY );
		} else if (targetY < 140) {
			targetY = (int) 140;
			faceEnemyAttacker = true;
			angleToEnemyAttacker = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, enemyAttackerRobotX, enemyAttackerRobotY );			
		}
		
		// Correct for defender plate not being in centre of robot
		//targetY += defenderOffset;
		
		float dist = targetY - defenderRobotY;
		
		//if move to right = negative, move to left = positive
		// if we are shooting right and we need to move right sideways, the distance is positive so convert it to be negative.
		if(!worldState.weAreShootingRight) {
			dist = -dist;
		}
		
		double angle = 0;

		
		
		boolean move_sideways = false;
		
		if (Math.abs(dist) >25) {
			move_sideways = true;
		}
		//System.out.println("Distance: " + dist);
		/*else {
			if(worldState.weAreShootingRight && defenderOrientation > 180){
				angle = 360 - defenderOrientation;
			}
			else if (worldState.weAreShootingRight && defenderOrientation <180) {
				angle = - defenderOrientation;
			}
			else {
				angle = 180 - defenderOrientation;
			}
		}
		
		if (Math.abs(angle) >20) {
			rotate  = true;
		}*/
		
		//RobotDebugWindow.messageDefender.setMessage("E("+enemyAttackerRobotX+","+enemyAttackerRobotY+")"+"D("+defenderRobotX+","+defenderRobotY+") "+"Enemy_angle:"+enemyAttackerOrientation+" Aim at:"+targetY+" Defender needs move:"+dist);
		
		synchronized (this.controlThread) {
			if(rotate){
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
           	 	System.out.println("DEFCHECK" + angleToDefCheck);
           	 	if (angleToDefCheck > 59){
           		 controlThread.operation.angleDifference = (int) (angleToDefCheck* 0.3);
           	 	}else if(angleToDefCheck > 30){
           		 controlThread.operation.angleDifference = (int) (angleToDefCheck * 0.6);
           	 	}else{
           		 controlThread.operation.angleDifference = (int) (angleToDefCheck  );
           	 	}
				
			}
			else if(move_sideways) {
				System.out.println("MOVE" + dist);
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = (int) dist;
			}else if (faceEnemyAttacker){
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
	           	System.out.println("ENEMY" + angleToEnemyAttacker);
	           	if (angleToDefCheck > 59){
	           		controlThread.operation.angleDifference = (int) (angleToEnemyAttacker* 0.3);
	           	}else if(angleToDefCheck > 30){
	           		controlThread.operation.angleDifference = (int) (angleToEnemyAttacker * 0.6);
	           	}else{
	           		controlThread.operation.angleDifference = (int) (angleToEnemyAttacker );
	           	}
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
					double rotateBy;
					double travelDist;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.angleDifference;
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
						/*case DEFTRAVEL:
						 if (travelDist != 0) {
							brick.execute(new RobotCommand.Trave(travelDist));
						}
						break;*/
					case DESIDEWAYS:
						if (travelDist != 0) {

							brick.execute(new RobotCommand.TravelSideways(travelDist));
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

