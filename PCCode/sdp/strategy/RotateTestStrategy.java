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

public class RotateTestStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	boolean isReady = false;
	
	

	public RotateTestStrategy(BrickCommServer brick) {
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
		
		
		double dx = ballX - attackerRobotX;
		double dy = ballY - attackerRobotY;	
		double targetAngle = calcTargetAngle(dx, dy);
		double angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
		
		
		boolean rotate = false;
		if(Math.abs(angleDifference) > 15)
		{
			rotate = true;
		}
		
		
		
		
		
		
		
		
		synchronized (this.controlThread) {
			
			//this.controlThread.worldState = worldState;
			
			if(rotate){
				this.controlThread.operation.op = Operation.Type.DEFROTATE;
				controlThread.operation.angleDifference = angleDifference;
				
				
			}
			else{
				this.controlThread.operation.op = Operation.Type.STOP;
				
			}
			
		
			
			
		}
	}
	
	protected class ControlThread extends Thread {
		public WorldState worldState;
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
					WorldState worldState;
					synchronized (this) {
						op = this.operation.op;
						rotateBy = this.operation.angleDifference;
						travelDist = this.operation.travelDistance;
						
						worldState = this.worldState;
					}
					
					
					
					//System.out.println("operation: " + op);
					switch (op) {
					case STOP:
						brick.robotController.stop();
						//RobotDebugWindow.messageAttacker.setMessage("STOP");
						break;
					case DEFROTATE:
						brick.robotController.rotate(rotateBy);
						//RobotDebugWindow.messageAttacker.setMessage("Rotating: "+angleDifference);
						break;
					case DEFTRAVEL:
						 brick.execute(new RobotCommand.Travel(travelDist));
						break;
					case DESIDEWAYS:
						brick.execute(new RobotCommand.TravelSideways(travelDist));
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