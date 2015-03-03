package sdp.strategy;
import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
//import sdp.strategy.LateNightDefenderStrategy.ControlThread;
import sdp.vision.Vector2f;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;

public class EarlyNightStrategy extends GeneralStrategy {
	private BrickCommServer brick;
	private BrickCommServer brickDef;
	ControlThread controlThread;
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	
    public EarlyNightStrategy(BrickCommServer brick){
    	this.brick = brick;    	
    }
    
    public void moveToDestination(Point destination){
    	
    }
    public void sendWorldState(WorldState worldState) {
		super.sendWorldState(worldState);
		// Calculate
		
		MovingObject ball = worldState.getBall();
		MovingObject robotDef = worldState.getDefenderRobot();
		MovingObject robotAtt = worldState.getAttackerRobot();
		//init the controlBox
		//WorldStateControlBox controlBox = worldState.getControlBox();
		
		//get the best "passing destinations"for each robot
		Point2 defDestination = ControlBox.controlBox.getDefenderPosition();
		Point2 attDestination = ControlBox.controlBox.getAttackerPosition();
		
		
		//if ball in defender area, 
		//boolean ballInDefenderArea = (worldState.weAreShootingRight == ballX > defenderCheck);
		robotDef
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
		boolean rotate_defender = false;
		
		double angleRR = defenderOrientation;
		double dx = ballX2 - defenderRobotX;
		double dy = ballY2 - defenderRobotY;
		double targetAngle = calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attDestination.getX(), attDestination.getY());
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
		
		/*if(Math.abs(angleDifference) > 25.0 ) {
			rotate_defender = true;
			//System.out.println("Need to rotate the robot because orientation=" + defenderOrientation);
			
		}*/
		
		
		boolean move_robot = false;
		
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
		if (defenderRobotY != defDestination.getY()){
			move_robot = true;
		}
		
		
		if (calculateAngle(defenderRobotX, defenderRobotY, defenderOrientation, attDestination.getX() , attDestination.getY()) > 25){
			rotate_defender = true;
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
		
		
		/*synchronized (this.controlThread) {
			this.controlThread.operation.op = Operation.Type.DO_NOTHING;
			
			if(move_robot && canMove) {
				this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
				controlThread.operation.travelDistance = (int) (dY*0.5);
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
			else if(rotate_defender && canMove) {
				
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.rotateBy = (int) (angleDifference);
			}
		}*/
    }
	

}
