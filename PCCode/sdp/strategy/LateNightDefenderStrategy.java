package sdp.strategy;

import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;

public class LateNightDefenderStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	boolean isReady = false;
	
	

	public LateNightDefenderStrategy(BrickCommServer brick) {
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
		// Calculate
		
		MovingObject ball = worldState.getBall();
		MovingObject robot = worldState.getDefenderRobot();
		//init the controlBox
		//WorldStateControlBox controlBox = worldState.getControlBox();
		
		//get the best "passing destinations"for each robot
		ControlBox.controlBox.computePositions(worldState);
		Point2 defDestination = ControlBox.controlBox.getDefenderPosition();
		Point2 attDestination = ControlBox.controlBox.getAttackerPosition();
		
		
		
		boolean ballInAttackerArea = false;
		if (ballX > leftCheck && ballX < rightCheck) {
			ballInAttackerArea = true;
		}
		boolean ballInDefenderArea = (ballX < defenderCheck == worldState.weAreShootingRight);
		
		ballPositions.addLast(new Vector2f(ball.x, ball.y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		boolean canMove = true;
		
		/*if (ballX < rightCheck) {
			canMove = true;
			//System.out.println(rightCheck+" "+ballX);
			
		}*/
		
		
		Vector2f ball3FramesAgo = ballPositions.getFirst();
		float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
		float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

		double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
		double c = ballY1 - slope * ballX1;
		boolean ballMovement = Math.abs(ballX2 - ballX1) < 10;
		//int targetY = (int) (slope * defenderRobotX + c);
		//double ang1 = calculateAngle(defenderRobotX, defenderRobotY,
		//		defenderOrientation, defenderRobotX, defenderRobotY - 50);
		//ang1 = ang1 / 3;
				
		// 1. Check if the robot needs to rotate
		
		//System.out.println("Orientation " + defenderOrientation + "; " + attackerOrientation);
		//boolean should_move = false;
		boolean rotate = false;
		double targetAngle;		
		//boolean facingTeamMate;
		
		/*double angleRR = defenderOrientation;
		double dx = ballX2 - defenderRobotX;
		double dy = ballY2 - defenderRobotY;
		double targetAngle = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attDestination.getX(), attDestination.getY());
		if(targetAngle < 0){
			targetAngle += 360;
		}
		//System.out.println(targetAngle);
		//System.out.println(defenderOrientation);
		double angleDifference = (targetAngle - defenderOrientation) % 360;
		*/double dx;
		double dy;
		
		if(!ballCaughtDefender && ballInDefenderArea){
			dx = ballX2 - defenderRobotX;
			dy = ballY2 - defenderRobotY;	
			targetAngle = calcTargetAngle(dx, dy);
		}else{
			//
			dx = defDestination.getX() - defenderRobotX;
			dy = defDestination.getY() - defenderRobotY;
			targetAngle = calcTargetAngle(dx, dy);
		}
		double angleDifference = calcAngleDiff(defenderOrientation, targetAngle);
		/*if(angleDifference < 0) {
			angleDifference += 360;
		}
		
		if(angleDifference > 180) {
			angleDifference -= 360;
		}*/
		
		//System.out.println("Angle difference: "+angleDifference);
		
		if(Math.abs(angleDifference) > 25.0 ) {
			rotate = true;
			//System.out.println("Need to rotate the robot because orientation=" + defenderOrientation);
			
		}
		
		
		double dX = ball3FramesAgo.x - defenderRobotX;
		int targetY = (int) (slope * dX + c);

		//System.out.println("Ball X: " + ball.x + " y " + ball.y);
		//System.out.println("Robot x" + defenderRobotX + " y " + defenderRobotY);
		int dY = (int) (defDestination.getY() - defenderRobotY);
		//if(Math.abs(dY) > 5) {
			//move_robot = true;
			//System.out.println("Need to move the robot since dY=" + dY);
	//	}
		//move the robot along the y axis
		
		
		/*if (calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attDestination.getX() , attDestination.getY()) > 25){
			rotate = true;
		}*/
		
		
		double targetDistance = Math.sqrt(dx*dx+dy*dy);
		double angleDiffToTeamMate = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attDestination.getX(), attDestination.getY());		
		double catchThreshold = 35;
		boolean catch_ball = false;
		boolean kick_ball = false;
		boolean uncatch = false;
		double angleTollerance = 25;
		boolean isTeamMateReady = ControlBox.controlBox.isDefenderReady();
		//System.out.println("bds "+ballDistanceSq);
		if(ballInDefenderArea && (targetDistance < catchThreshold) && !ballCaughtDefender) {
            //System.out.println("Catching: "+ballDistance);
            catch_ball = true;
        }
		else if(ballCaughtDefender && !kicked && isTeamMateReady && !super.isBallPassed){
			//System.out.println("Kicking");
            // Here: need to check if the defender is ready and we don't need to move any further
			kick_ball= true;			
		}else if((Math.abs(targetDistance) < catchThreshold) && (Math.abs(angleDiffToTeamMate) > angleTollerance)){
			rotate = true;
			angleDifference = angleDiffToTeamMate;
			
		}else if((Math.abs(targetDistance) < catchThreshold) && (Math.abs(angleDiffToTeamMate) < angleTollerance)){
			this.isReady = true;
		}
		
		
		
		
		
		boolean move_robot = false;
		
		
		if(targetDistance > 25) {
			move_robot = true;
			//System.out.println("Need to move the robot since dY=" + dY);
		}

		
		
		
		
		
		
		
		boolean move_back = false;
		
		double checkDx = defenderRobotX - defenderCheck;
		//System.out.println(checkDx);
		if(Math.abs(checkDx) < 40 ){
			//move_back = true;
			//System.out.println("Move back");
		}
		
		
		synchronized (this.controlThread) {
			this.controlThread.operation.op = Operation.Type.DO_NOTHING;
			
			 if(rotate) {
					this.controlThread.operation.op = Operation.Type.DEFROTATE;
					controlThread.operation.rotateBy = (int) (angleDifference);
				}
				else if(catch_ball){
					//System.out.println("Catch");
					this.controlThread.operation.op = Operation.Type.DEFCATCH;
				}
				else if(kick_ball){
					//System.out.println("Kick");
					this.controlThread.operation.op = Operation.Type.DEFKICK;
					super.isBallPassed = true;
				}
				else if(uncatch){
					//System.out.println("Uncatch");
					this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
				}
				else if(catch_ball){
					System.out.println("Catch");
					this.controlThread.operation.op = Operation.Type.DEFCATCH;
				}
				else if(move_back) {
					this.controlThread.operation.op = Operation.Type.DEBACK;
					controlThread.operation.travelDistance = (int) Math.min(-10, -(40-Math.abs(checkDx)));
				}
				else if(move_robot) {
					this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
					controlThread.operation.travelDistance = (int) targetDistance;
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
					System.out.println("operation: " + op + " rotateBy: "
							 + rotateBy + " travelDist: " + travelDist);
					switch (op) {
					case DEFROTATE:
						if (rotateBy != 0) {
						brick.executeSync(new RobotCommand.Rotate(
								rotateBy, Math.abs(rotateBy)));
						}
						break;
					case DEFTRAVEL:
						 if (travelDist != 0) {
							brick.execute(new RobotCommand.Travel(
									travelDist / 3,
									Math.abs(travelDist) * 3 + 25));
						}
						break;
					case DESIDEWAYS:
						if (travelDist != 0) {
							brick.execute(new RobotCommand.TravelSideways(
									travelDist / 3,
									Math.abs(travelDist) * 3 + 25));
						}
						break;
					case DEBACK:
						if (travelDist != 0) {
							brick.execute(new RobotCommand.Travel(
									travelDist,
									travelDist));
						}
						break;
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
							brick.execute(new RobotCommand.Kick(0));
							brick.execute(new RobotCommand.ResetCatcher());
							
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
