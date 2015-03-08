package sdp.strategy;

import sdp.strategy.*;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.world.Pitch;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;

public class ControlBox implements WorldStateControlBox {

	public static ControlBox controlBox = new ControlBox();

	private boolean avoid = true;
	private boolean isAttackerReady = true;
	private boolean orth = true;
	private Point2 attPos = new Point2(0, 0);
	private Point2 defPos = new Point2(0, 0);
	private Point2 shotPos = new Point2(0, 0);
	private double shotAngle = 0.0;
	private boolean comp = false;
	// /////////////////DEBUG//////////////////////////
	private boolean DEBUG = false;

	// ///////////////////////////////////////////////

	private ControlBox() {
	}

	// If it has already been computed and still works, just return the value.
	// If not, compute a new one based on the position of the enemies attacker.
	public int getOrthogonal(WorldState ws) {
		float oppY = ws.getEnemyAttackerRobot().y;
		float pitchY = ws.getPitch().getPitchHeight();
		if (oppY > pitchY / 2.0) {
			return (int) (oppY / 2);
		} else {
			return (int) (pitchY - (oppY / 2));
		}

	}

	// Orth set/get
	public void setOrthogonal(boolean o) {
		this.orth = o;
	}

	public boolean getOrthogonal() {
		return orth;
	}

	// Obstacle set/get
	public void avoidObstacle(boolean shouldAvoidObstacle) {
		avoid = shouldAvoidObstacle;
	};

	public boolean getAvoidObstacle() {
		return avoid;
	}

	public void computePositions(WorldState ws) {
		if (comp) {
			if (DEBUG) {
				System.out
						.println("CONTROL BOX: Values are ready for use, won't recompute.");
			}
			return;
		}
		if (!avoid) {
			float pitchY = ws.getPitch().getPitchHeight();
			defPos = new Point2((int) (ws.getDefenderRobot().x), pitchY / 2);
			attPos = new Point2((int) (ws.getAttackerRobot().x), pitchY / 2);
			comp = true;
			if (DEBUG) {
				System.out
						.println("CONTROL BOX: No need to avoid obstacle as per set flag, returning	 middle of pitch.");
			}
			return;
		}
		if (orth) {
			int ypos = getOrthogonal(ws);
			defPos = new Point2((int) (ws.getDefenderRobot().x), ypos);
			attPos = new Point2((int) (ws.getAttackerRobot().x), ypos);
			if (DEBUG) {
				System.out
						.println("CONTROL BOX: Passing orthogonally at same x, and at y: "
								+ ypos);
			}
		} else {
			/*
			 * Passing at non orhtogonal angles would be done here for proper
			 * play, however the intereface and the interraction should be
			 * updated for this to work.
			 */
			if (DEBUG) {
				System.out.println("CONTROL BOX: Passing at non-orthogonal");
			}
		}
		comp = true;
	};

	// This may need to add a delta range depending if it is possible to move
	// with 1 unit precision.
	public boolean shouldDefenderMove(WorldState ws) {
		return !(ws.getAttackerRobot().y == attPos.getY() && ws
				.getAttackerRobot().x == attPos.getX());
	}

	public Point2 getAttackerPosition() {
		return attPos;
	}

	public Point2 getDefenderPosition() {
		return defPos;
	}

	/*
	 * Once the defender is in the X position and ready to catch, it should tell
	 * the attacker to kick.
	 */
	public void setAttackerReady() {
		isAttackerReady = true;
	}

	public boolean isAttackerReady() {
		return isAttackerReady;
	}

	// Returns true if robots can pass in current situation, currently returns
	// true if diff between Y values is less than 100 and obstacle is more than
	// 100 away.
	public boolean canPass(WorldState ws) {
		if (orth) {
			if (DEBUG) {
				System.out
						.println("CONTROL BOX: Checking if orthogonal pass is possible");
			}
			return canPassOrth(ws);
		} else {
			if (DEBUG) {
				System.out
						.println("CONTROL BOX: Checking if line between defender and attacker is empty.");
			}
			return lineClear(ws);
		}
	}

	private boolean canPassOrth(WorldState ws) {
		float defY = ws.getDefenderRobot().y;
		float oppY = ws.getEnemyAttackerRobot().y;
		float attY = ws.getAttackerRobot().y;
		return Math.abs(defY - attY) < 100 && Math.abs(defY - oppY) > 100
				&& Math.abs(attY - oppY) > 100;
	}

	private boolean lineClear(WorldState ws) {
		double defX = ws.getDefenderRobot().x;
		double defY = ws.getDefenderRobot().y;
		double attX = ws.getAttackerRobot().x;
		double attY = ws.getAttackerRobot().y;
		double oppX = ws.getEnemyAttackerRobot().x;
		double oppY = ws.getEnemyAttackerRobot().y;
		double attAngle = calcTargetAngle(Math.abs(defX - attX),
				Math.abs(defY - attY));
		double oppAngle = calcTargetAngle(Math.abs(defX - oppX),
				Math.abs(defY - oppY));
		if (DEBUG) {
			System.out.println("CONTROL BOX: Angle between obstacle: "
					+ oppAngle + " Angle between our robots: " + attAngle);
		}
		double ballSize = Pitch.BALL_RADIUS;
		double robotSize = ballSize * 6;
		double neededMargin = robotSize + ballSize;
		double margin = calcMargin(ws.getDefenderRobot(),
				ws.getEnemyAttackerRobot(), oppAngle - attAngle);
		if (DEBUG) {
			System.out.println("CONTROL BOX: Required margin: " + neededMargin
					+ " Margin at current pos: " + margin);
		}
		return margin > neededMargin;
	}

	public void reset() {
		if (DEBUG) {
			System.out.println("CONTROL BOX: Reset.");
		}
		comp = false;
		isAttackerReady = false;
		attPos = new Point2(0, 0);
		defPos = new Point2(0, 0);
		shotPos = new Point2(0, 0);
		shotAngle = 0.0;
	}

	public boolean computed() {
		return comp;
	}

	@Override
	public Point2 getShootingPosition() {
		// TODO Auto-generated method stub
		return shotPos;
	}

	@Override
	public double getShootingAngle() {
		// TODO Auto-generated method stub
		return shotAngle;
	}

	// Returns angles between the robots
	private double calcTargetAngle(double dx, double dy) {
		double targetAngle = Math.toDegrees(Math.atan2(dy, dx)) % 360;

		if (targetAngle < 0) {
			targetAngle += 360;
		}

		return targetAngle;
	}

	// Returns how big the margin is based on the angle. Needed to check if we
	// won't graze the robot
	// when passing in current situation.
	private double calcMargin(MovingObject def, MovingObject opp, double alpha) {
		double dist = Math.sqrt((def.x - opp.x) * (def.x - opp.x)
				+ (def.y - opp.y) * (def.y - opp.y));
		double size = Math.sin(alpha) * dist;
		return size;
	}

	@Override
	public void computeShot(WorldState ws) {
		// NOTE! Currently just computes angle based on current position.
		shotAngle = computeStaticAngle(ws);
		MovingObject att = ws.getAttackerRobot();
		shotPos = new Point2(att.x, att.y);
	}

	private double computeStaticAngle(WorldState ws) {
		//Get positions.
		MovingObject att = ws.getAttackerRobot();
		MovingObject opp = ws.getEnemyDefenderRobot();
		//ws.getPitch().
		//Find furthest corner.
		//Get the angle from our position.
		return 0.0;
	}
}
