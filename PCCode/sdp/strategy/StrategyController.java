package sdp.strategy;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import sdp.comms.packets.*;
import sdp.comms.SingletonRadio;
import sdp.comms.BrickCommServer;
import sdp.strategy.interfaces.Strategy;
import sdp.vision.interfaces.WorldStateReceiver;
import sdp.world.oldmodel.WorldState;
import sdp.util.DriveDirection;

public class StrategyController implements WorldStateReceiver {

	/** Measured in milliseconds */
	public static final int STRATEGY_TICK = 100;
			; //100; // TODO: Test lower values for this and see where it breaks
	
	public enum StrategyType {
		DO_SOMETHING, DO_NOTHING, PASSING, ATTACKING, DEFENDING, MARKING, MILESTONE_TWO_A, MILESTONE_TWO_B, MILESTONE_THREE_A, MILESTONE_THREE_B, CALIBRATION
	}
	
	public enum BallLocation{
		DEFENDER, ATTACKER, ENEMY_DEFENDER, ENEMY_ATTACKER
	}
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public Object bcsTemp;
	public BrickCommServer bcsDefender, bcsAttacker;
	private BallLocation ballLocation;
	private StrategyType currentStrategy = StrategyType.DO_NOTHING;
	private boolean pauseStrategyController = true;
	private Strategy ats, dfs;
	
	// Advanced Tactics flags
	public static boolean confusionEnabled = false;
	public static boolean bounceShotEnabled = false;
	public static boolean interceptorDefenceEnabled = false;
	public static boolean bouncePassEnabled = false;
	
	private static ArrayList<Strategy> currentStrategies = new ArrayList<Strategy>();
	private static ArrayList<Strategy> removedStrategies = new ArrayList<Strategy>();

	public StrategyController() {
		this.bcsTemp = new Object();
		this.bcsAttacker = new BrickCommServer("attacker");
        this.bcsDefender = new BrickCommServer("defender");
        //Check which one is the attacker and defender and assign appropriately
		// TODO: Devise a non-hanging way of doing this
		/*if (bcsAttacker.isAttacker() == false || bcsDefender.isAttacker() == true) {
			bcsTemp = bcsAttacker;
			bcsAttacker = bcsDefender;
			bcsDefender = (BrickCommServer) bcsTemp;
		}*/
        //System.out.println();
	}

	public StrategyType getCurrentStrategy() {
		return currentStrategy;
	}

	public boolean isPaused() {
		return pauseStrategyController;
	}

	public void setPaused(boolean paused) {
		boolean oldValue = pauseStrategyController;
		pauseStrategyController = paused;
		pcs.firePropertyChange("paused", oldValue, paused);
	}	

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public static ArrayList<Strategy> getCurrentStrategies() {
		return currentStrategies;
	}

	public static ArrayList<Strategy> getRemovedStrategies() {
		return removedStrategies;
	}

	public static void setRemovedStrategies(
			ArrayList<Strategy> removedStrategies) {
		StrategyController.removedStrategies = removedStrategies;
	}

	/**
	 * Change to a particular strategy, removing and stopping the previously
	 * running strategy(s).
	 * 
	 * @param type
	 *            - The strategy type to run
	 */
	public void changeToStrategy(StrategyType type) {
		// Stop old threads
		for (Strategy s : StrategyController.currentStrategies) {
			s.stopControlThread();
			StrategyController.removedStrategies.add(s);
		}
		StrategyController.currentStrategies = new ArrayList<Strategy>();
		//SingletonRadio radio = new SingletonRadio("/dev/ttyACM0");
		switch (type) {
		case DO_SOMETHING:	
			//radio.sendPacket(new ActivatePacket());
			
			break;
        case MILESTONE_THREE_A:
            // Without the obstacle, simple version
        	ControlBox.controlBox.avoidObstacle(true);
        	Strategy m3as = new Milestone3AttackingStrategy(this.bcsAttacker);
        	StrategyController.currentStrategies.add(m3as);
        	m3as.startControlThread();
        	break;
        	
        case MILESTONE_THREE_B:
            // With the obstacle, extended version
        	//ControlBox.controlBox.avoidObstacle(false);
        	Strategy m3ds = new Milestone3DefendingStrategy(this.bcsDefender);
        	StrategyController.currentStrategies.add(m3ds);
        	m3ds.startControlThread();
            break;
            
        case MILESTONE_TWO_A:
        	ats = new SidewaysTestStrategy(this.bcsAttacker);
            StrategyController.currentStrategies.add(ats);
            ats.startControlThread();
            
            break;
        case MILESTONE_TWO_B:
            ats = new RotateTestStrategy(this.bcsAttacker);
            StrategyController.currentStrategies.add(ats);
            ats.startControlThread();
            
            break;
        case CALIBRATION:
            //ats = new CalibrationStrategy(this.bcsAttacker);
            //dfs = new CalibrationStrategy(this.bcsDefender);
            StrategyController.currentStrategies.add(ats);
            StrategyController.currentStrategies.add(dfs);
            ats.startControlThread();
            dfs.startControlThread();
            
            break;
		case DO_NOTHING:
			byte stop = 0;
			DriveDirection fw = DriveDirection.FORWARD;
			//radio.sendPacket(new DrivePacket(stop, fw, stop, fw, stop, fw, 0));
			break;
		case PASSING:
			Strategy ps = new PassingStrategy(this.bcsAttacker,
					this.bcsDefender);
			StrategyController.currentStrategies.add(ps);
			ps.startControlThread();
			break;
		case ATTACKING:
			Strategy as = new AttackerStrategy(this.bcsAttacker);
			Strategy ic = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(as);
			StrategyController.currentStrategies.add(ic);
			as.startControlThread();
			ic.startControlThread();
			break;
		case DEFENDING:
			//Strategy AS = new AttackerStrategy(this.bcsAttacker);
			//StrategyController.currentStrategies.add(AS);
			//AS.startControlThread();			
			Strategy nis = new NewInterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(nis);
			nis.startControlThread();			
			break;
		case MARKING:
			/*Strategy newMar = new newMarkingStrategy(this.bcsAttacker);
			Strategy ics = new InterceptorStrategy(this.bcsDefender);
			StrategyController.currentStrategies.add(newMar);
			StrategyController.currentStrategies.add(ics);
			newMar.startControlThread();
			ics.startControlThread();*/
			break;
		default:
			break;
		}
		StrategyType oldType = currentStrategy;
		currentStrategy = type;
		pcs.firePropertyChange("currentStrategy", oldType, currentStrategy);
	}

	@Override
	public void sendWorldState(WorldState worldState) {
		if (pauseStrategyController)
			return;
		// Check where the ball is, and make a decision on which strategies to
		// run based upon that.
		int defenderCheck = (worldState.weAreShootingRight) ? worldState.dividers[0]
				: worldState.dividers[2];
		int leftCheck = (worldState.weAreShootingRight) ? worldState.dividers[1]
				: worldState.dividers[0];
		int rightCheck = (worldState.weAreShootingRight) ? worldState.dividers[2]
				: worldState.dividers[1];
		float ballX = worldState.getBall().x;
		// Mark zone the ball was in on the previous frame.
		BallLocation prevBallLocation = this.ballLocation;

		// Find where the ball is located on the pitch
		if ((worldState.weAreShootingRight && ballX < defenderCheck)
				|| (!worldState.weAreShootingRight && ballX > defenderCheck)) {
			this.ballLocation = BallLocation.DEFENDER;
		} else if (ballX > leftCheck && ballX < rightCheck) {
			this.ballLocation = BallLocation.ATTACKER;
		} else if (worldState.weAreShootingRight && ballX > defenderCheck
				&& ballX < leftCheck || !worldState.weAreShootingRight
				&& ballX < defenderCheck && ballX > rightCheck) {
			this.ballLocation = BallLocation.ENEMY_ATTACKER;
		} else if (!worldState.weAreShootingRight && (ballX < leftCheck)
				|| worldState.weAreShootingRight && (ballX > rightCheck)) {
			this.ballLocation = BallLocation.ENEMY_DEFENDER;
		}

		// Change strategy only if the ball has changed pitch area.
		if (prevBallLocation != ballLocation){
			
			switch(this.ballLocation){
			case ATTACKER:
				changeToStrategy(StrategyType.ATTACKING);
				break;
			case DEFENDER:
				changeToStrategy(StrategyType.MILESTONE_THREE_B);
				break;
			case ENEMY_ATTACKER:
				changeToStrategy(StrategyType.DEFENDING);
				break;
			case ENEMY_DEFENDER:
				changeToStrategy(StrategyType.MARKING);
				break;
			}
		}
	}
}

