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

public class CalibrationStrategy extends GeneralStrategy {

	
	private BrickCommServer brick;
	private ControlThread controlThread;
	private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
	
	private long caughtTime;
	private long kickTime;
	private boolean kicked = false;
	private boolean ballCaught = false;
	boolean isReady = false;
	boolean attacker = false;
	
	private MovingObject oldState, newState;
	
	
	

	public CalibrationStrategy(BrickCommServer brick) {
		this.brick = brick;
		this.controlThread = new ControlThread();
		attacker = brick.name.equals("attacker");
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
		if(newState == null){
			if(attacker){
				newState = worldState.getAttackerRobot().copy();
			} else {
				newState = worldState.getDefenderRobot().copy();
			}
		}
		
		
		//MinPower
		//travel
		//travelSideways
		//rotate
		
		
		
		
		synchronized (this.controlThread) {
			
			
			
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