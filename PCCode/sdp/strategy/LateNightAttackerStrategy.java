package sdp.strategy;

import java.util.ArrayDeque;
import sdp.world.oldmodel.Point2;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.WorldState;
import sdp.strategy.interfaces.*;

public class LateNightAttackerStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	private boolean kicked;
	private boolean isReady = false;
	
	

	public LateNightAttackerStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
	}
	public boolean isRobotReady(){
		return isReady;
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
		// Calculate
		
		MovingObject ball = worldState.getBall();
		MovingObject robot = worldState.getAttackerRobot();
		
		//get the best "passing destinations"for each robot
		/*if(!ControlBox.controlBox.computed()){
			ControlBox.controlBox.computePositions(worldState);
			//System.out.println("Robot destinations computed");
		}
		Point2 attackerPosition = ControlBox.controlBox.getAttackerPosition();
		Point2 defenderPosition = ControlBox.controlBox.getDefenderPosition();
		*/
		//boolean ballInDefenderArea = (ballX < defenderCheck == worldState.weAreShootingRight);
		ballPositions.addLast(new Vector2f(ball.x, ball.y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();
		boolean ballInAttackerArea = false;
		////System.out.println(" Ball is in att are : " + ballInAttackerArea);
		if (ballX > leftCheck && ballX < rightCheck) {
			ballInAttackerArea = true;
			
			//System.out.println(" Ball is in att area : " + ballInAttackerArea);
		}
		
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
		boolean isTeamMateReady = ControlBox.controlBox.isAttackerReady();
		boolean rotate = false;
		double targetAngle;		
		boolean facingTeamMate;
		
		
		double dx;
		double dy;
		
		//if(!ballCaughtAttacker && ballInAttackerArea){
			dx = ballX2 - attackerRobotX;
			dy = ballY2 - attackerRobotY;
			
			targetAngle = calcTargetAngle(dx, dy);
		//}
		/*else if(!ballInAttackerArea){
			dx = goalX - attackerRobotX;
			dy = goalY[1] - attackerRobotY;
			targetAngle = calcTargetAngle(dx, dy);
			
		}*/ 
		/*else{
			dx = attackerPosition.getX()- attackerRobotX;
			dy = attackerPosition.getY() - attackerRobotY;
			targetAngle = calcTargetAngle(dx, dy);
		}*/
		
		////System.out.println(targetAngle);
		////System.out.println(defenderOrientation);
		double angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
		
		
		////System.out.println("Angle difference: "+angleDifference);
		double angleTollerance = 20.0;
		if(Math.abs(angleDifference) > angleTollerance ) {
			rotate = true;
			//System.out.println("We need to rotate " + angleDifference);
			//System.out.println(".........................................................................." + defenderOrientation);
			
		}
		
        //In this case ball distance is also the distance to the target position/destination
		double targetDistance = Math.sqrt(dx*dx+dy*dy);
		double angleDiffToTeamMate = calculateAngle(attackerRobotX, attackerRobotY, attackerOrientation, defenderRobotX, defenderRobotY);
		
		double catchThreshold = 30;
		boolean catch_ball = false;
		boolean kick_ball = false;
		boolean uncatch = false;
		
		
		////System.out.println("bds "+ballDistanceSq);
		
		if(ballInAttackerArea && Math.abs(targetDistance)< catchThreshold && !ballCaughtAttacker) {
            ////System.out.println("Catching: "+ballDistance);
            catch_ball = true;
        }
		else if(ballCaughtAttacker && !kicked && isReady){
			////System.out.println("Kicking");
            // Here: need to check if the defender is ready and we don't need to move any further
			kick_ball= true;			
		}else if(/*Math.abs(targetDistance) < catchThreshold*/ Math.abs(angleDiffToTeamMate) > angleTollerance && ballCaughtAttacker){
			rotate = true;
			angleDifference = angleDiffToTeamMate;
			//System.out.println("We have reached the destination given by ControlBox");
		}else if((Math.abs(targetDistance)< catchThreshold) && (Math.abs(angleDiffToTeamMate) < angleTollerance)){
			this.isReady = true;
			kick_ball= true;
			//System.out.println("The robot has reahced the desired position");
		}
		
		
		
		
		
		boolean move_robot = false;
		
		
		if(Math.abs(targetDistance) > 25 && ballInAttackerArea) {
			move_robot = true;
			//System.out.println("we need to move, Ball is in attacker area");
			////System.out.println("Need to move the robot since dY=" + dY);
		}//else if(targetDistance )
		
		
		
		
		
		
		
		
		boolean move_back = false;
		
		double checkAxRight = Math.abs(attackerRobotX - rightCheck);
		double checkAxLeft = Math.abs(attackerRobotX - leftCheck);
		////System.out.println(checkDx);
		if(checkAxRight < 20  || checkAxLeft < 20){
			move_back = true;
			//System.out.println("Move back");
		}
		
		
		synchronized (this.controlThread) {
			this.controlThread.operation.op = Operation.Type.DO_NOTHING;
			
			/*this.controlThread.operation.op = Operation.Type.MOVENROTATE;
			controlThread.operation.dA = angleDifference;
			controlThread.operation.dX = dx;
			controlThread.operation.dY = dy;
			
			*/
		    if(rotate) {
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.rotateBy = (int) (angleDifference);
				rotate = false;
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
			else if(catch_ball){
				//System.out.println("Catch");
				this.controlThread.operation.op = Operation.Type.DEFCATCH;
				ballCaughtAttacker = true;
				
			}
			else if(move_back) {
				this.controlThread.operation.op = Operation.Type.DEBACK;
				controlThread.operation.travelDistance = (int) -40;
			}
			else if(move_robot) {
				//stem.out.println("A: ");
				this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
				controlThread.operation.travelDistance = (int) targetDistance;
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
					double dX, dY, dA;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.rotateBy;
						travelDist = this.operation.travelDistance;
						
						
						dX = this.operation.dX;
						dY = this.operation.dY;
						dA = this.operation.dA ;
					}
					
					
					switch (op) {
					
					case DEFROTATE:
						if (rotateBy != 0) {
						//System.out.println("A: ");
						brick.executeSync(new RobotCommand.Rotate(
								rotateBy));
						}
						break;
					case DEFTRAVEL:
						 if (travelDist != 0) {
							brick.execute(new RobotCommand.Travel(
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
							brick.execute(new RobotCommand.Travel(
									travelDist));
						}
					case DEFCATCH:
						if((System.currentTimeMillis() - kickTime > 3000)){
							//System.out.println("Catching");
							
							
							brick.execute(new RobotCommand.Catch());
							ballCaughtAttacker = true;
							caughtTime = System.currentTimeMillis();
							kicked = false;
						}
						break;
					case DEFKICK:
						if((System.currentTimeMillis() - caughtTime > 1000)){
							//System.out.println("Kicking");
							
							brick.execute(new RobotCommand.Kick());
							
							kicked = true;
							ballCaughtAttacker = false;
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
