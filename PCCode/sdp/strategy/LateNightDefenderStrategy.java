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

public class LateNightDefenderStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	boolean isReady = false;
	boolean edwin_is_ball_defender_caught_dari = false;
	

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
		
		/*if(!ControlBox.controlBox.computed()){
			ControlBox.controlBox.computePositions(worldState);
		}
		Point2 defDestination = ControlBox.controlBox.getDefenderPosition();
		Point2 attDestination = ControlBox.controlBox.getAttackerPosition();
		*/
		
		
		//boolean ballInAttackerArea = false;
		/*if (ballX > leftCheck && ballX < rightCheck) {
			ballInAttackerArea = true;
			//System.out.println("This is an attacker robot and the ball is at: " + ballX);
		}*/
		boolean ballInDefenderArea = (ballX < defenderCheck == worldState.weAreShootingRight);
		
		ballPositions.addLast(new Vector2f(ball.x, ball.y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		//boolean canMove = true;
		
		/*if (ballX < rightCheck) {
			canMove = true;
			////System.out.println(rightCheck+" "+ballX);
			
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
		
		////System.out.println("Orientation " + defenderOrientation + "; " + attackerOrientation);
		//boolean should_move = false;
		boolean rotate = false;
		double targetAngle;		
		//boolean facingTeamMate;
		
		
		double dx;
		double dy;
		////System.out.println("BALL " + ballX2 + " " + ballY2 + " DR " + defenderRobotX + " " + defenderRobotY);
		////System.out.println("BOOELANS " + edwin_is_ball_defender_caught_dari + " " + ballInDefenderArea);
		//if(!edwin_is_ball_defender_caught_dari && ballInDefenderArea){
			dx = ballX2 - defenderRobotX;
			dy = ballY2 - defenderRobotY;	
			targetAngle = calcTargetAngle(dx, dy);
			
			
			RobotDebugWindow.messageDefender.setMessage("Trying to catch the ball: "+calcAngleDiff(defenderOrientation, targetAngle));
			
			////System.out.println("BALLL DIRECTION...................");
			////System.out.println("BALL " + ballX2 + " " + ballY2 + " DR " + defenderRobotX + " " + defenderRobotY);
		/*}else{
			//
			dx = defDestination.getX() - defenderRobotX;
			dy = defDestination.getY() - defenderRobotY;
			targetAngle = calcTargetAngle(dx, dy);
			//System.out.println("DESTINATION#############################################");
		}*/
		
		double angleDifference = calcAngleDiff(defenderOrientation, targetAngle);
		////System.out.println("Point: " + defDestination.getX() + " " + defDestination.getY() + " Dx: " + dx + " dy: " + dy + " Def: " + defenderOrientation + " Target Angle: " + targetAngle + " Diff: " + angleDifference);
		
		/*if(angleDifference < 0) {
			angleDifference += 360;
		}
		
		if(angleDifference > 180) {
			angleDifference -= 360;
		}*/
		
		////System.out.println("Angle difference: "+angleDifference);
		
		if(Math.abs(angleDifference) > 25.0 ) {
			rotate = true;
			////System.out.println("Need to rotate the robot because orientation=" + defenderOrientation);
			
		}
		
		
		double dX = ball3FramesAgo.x - defenderRobotX;
		int targetY = (int) (slope * dX + c);

		////System.out.println("Ball X: " + ball.x + " y " + ball.y);
		////System.out.println("Robot x" + defenderRobotX + " y " + defenderRobotY);
		int dY = (int) (ballY - defenderRobotY);
		//if(Math.abs(dY) > 5) {
			//move_robot = true;
			////System.out.println("Need to move the robot since dY=" + dY);
	//	}
		//move the robot along the y axis
		
		
		/*if (calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attDestination.getX() , attDestination.getY()) > 25){
			rotate = true;
		}*/
		
		
		double targetDistance = Math.sqrt(dx*dx+dy*dy);
		double angleDiffToTeamMate = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attackerRobotX, attackerRobotY);		
		double catchThreshold = 35;
		boolean catch_ball = false;
		boolean kick_ball = false;
		boolean uncatch = false;
		double angleTollerance = 15;
		boolean isTeamMateReady = ControlBox.controlBox.isAttackerReady();
		////System.out.println("bds "+ballDistanceSq);
		if(ballInDefenderArea && (targetDistance < catchThreshold) && !ballCaughtDefender) {
            ////System.out.println("Catching: "+ballDistance);
            catch_ball = true;
        }
		else if(ballCaughtDefender && !kicked && isTeamMateReady && !super.isBallPassed){
			////System.out.println("Kicking");
            // Here: need to check if the defender is ready and we don't need to move any further
			kick_ball= true;			
		}else if(/*(Math.abs(targetDistance) < catchThreshold)8/ && */(Math.abs(angleDiffToTeamMate) > angleTollerance)){
			rotate = true;
			angleDifference = angleDiffToTeamMate;
			
		}else if((Math.abs(targetDistance) < catchThreshold) && (Math.abs(angleDiffToTeamMate) < angleTollerance)){
			this.isReady = true;
		}
		
		
		
		
		
		boolean move_robot = false;
		
		
		if(Math.abs(targetDistance) > 25) {
			move_robot = true;
			////System.out.println("Need to move the robot since dY=" + dY);
		}

		
		
		
		
		
		
		
		boolean move_back = false;
		
		double checkDx = defenderRobotX - defenderCheck;
		////System.out.println(checkDx);
		if(Math.abs(checkDx) < 40 ){
			//move_back = true;
			////System.out.println("Move back");
		}
		
		
		synchronized (this.controlThread) {
			this.controlThread.operation.op = Operation.Type.DO_NOTHING;
			
			 if(rotate) {
				 ////System.out.println("Roatet: " + angleDifference);
					this.controlThread.operation.op = Operation.Type.DEFROTATE;
					controlThread.operation.rotateBy = (int) (angleDifference);
				}
				else if(catch_ball){
					////System.out.println("Catch");
					this.controlThread.operation.op = Operation.Type.DEFCATCH;
				}
				else if(kick_ball){
					////System.out.println("Kick");
					this.controlThread.operation.op = Operation.Type.DEFKICK;
					super.isBallPassed = true;
				}
				else if(uncatch){
					////System.out.println("Uncatch");
					this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
				}
				
				else if(move_back) {
					////System.out.println("BW: " + Math.min(-10, -(40-Math.abs(checkDx))));
					this.controlThread.operation.op = Operation.Type.DEBACK;
					controlThread.operation.travelDistance = (int) Math.min(-10, -(40-Math.abs(checkDx)));
				}
				else if(move_robot) {
					////System.out.println("FW: " + targetDistance);
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
						break;
					case DEFCATCH:
						if((System.currentTimeMillis() - kickTime > 3000)){
							brick.execute(new RobotCommand.Catch());
							ballCaughtDefender = true;
							edwin_is_ball_defender_caught_dari = true;
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
