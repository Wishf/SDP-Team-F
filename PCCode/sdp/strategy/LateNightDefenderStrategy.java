package sdp.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.WorldState;

public class LateNightDefenderStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;

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
	
	@Override
	public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);
		// Calculate
		
		MovingObject ball = worldState.getBall();
		MovingObject robot = worldState.getDefenderRobot();
		
		
		boolean ballInAttackerArea = false;
		ballPositions.addLast(new Vector2f(ball.x, ball.y));
		if (ballPositions.size() > 3)
			ballPositions.removeFirst();

		boolean canMove = false;
		
		if (ballX < rightCheck) {
			canMove = true;
			//System.out.println(rightCheck+" "+ballX);
			
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
		
		//System.out.println("Orientation " + defenderOrientation + "; " + attackerOrientation);
		
		boolean rotate_defender = false;
		double angleRR = defenderOrientation;
		double dx = ballX2 - defenderRobotX;
		double dy = ballY2 - defenderRobotY;
		double targetAngle = 0;//Math.toDegrees(Math.atan2(dy, dx)) % 360;
		if(targetAngle < 0){
			targetAngle += 360;
		}
		//System.out.println(targetAngle);
		//System.out.println(defenderOrientation);
		double angleDifference = (targetAngle - defenderOrientation) % 360;
		
		if(angleDifference < 0) {
			angleDifference += 360;
		}
		
		if(angleDifference > 180) {
			angleDifference -= 360;
		}
		
		//System.out.println("Angle difference: "+angleDifference);
		
		if(Math.abs(angleDifference) > 25.0 ) {
			rotate_defender = true;
			//System.out.println("Need to rotate the robot because orientation=" + defenderOrientation);
			
		}
		
		
		boolean move_robot = false;
		
		double dX = ball3FramesAgo.x - defenderRobotX;
		int targetY = (int) (slope * dX + c);

		//System.out.println("Ball X: " + ball.x + " y " + ball.y);
		//System.out.println("Robot x" + defenderRobotX + " y " + defenderRobotY);
		int dY = (int) (targetY - defenderRobotY);
		if(Math.abs(dY) > 5) {
			move_robot = true;
			//System.out.println("Need to move the robot since dY=" + dY);
		}
		
		
		
		double ballDistanceSq = dx*dx+dy*dy;
		double catchThreshold = 700;
		boolean catch_ball = false;
		boolean kick_ball = false;
		boolean uncatch = false;
		
		//System.out.println("bds "+ballDistanceSq);
		
		if(ballDistanceSq < catchThreshold && !ballCaught){
			System.out.println("Catching: "+ballDistanceSq);
			catch_ball = true;
		}	
		else if(ballCaught && !kicked){
			System.out.println("Kicking");
			kick_ball= true;			
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
			
			if(rotate_defender && canMove) {
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
			}
			else if(uncatch){
				//System.out.println("Uncatch");
				this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
			}
			else if(move_back && canMove) {
				this.controlThread.operation.op = Operation.Type.DEBACK;
				controlThread.operation.travelDistance = -7;//Math.min(-5, -(40-Math.abs(checkDx)));
			}
			else if(move_robot && canMove) {
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = (int) (dY*0.5);
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
