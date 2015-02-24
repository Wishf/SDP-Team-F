package sdp.vision.recognisers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import sdp.vision.DistortionFix;
import sdp.vision.PitchConstants;
import sdp.vision.PixelInfo;
import sdp.vision.Position;
import sdp.vision.Vector2f;
import sdp.vision.Vision;
import sdp.vision.interfaces.ObjectRecogniser;
import sdp.vision.interfaces.PitchViewProvider;
import sdp.world.DynamicWorldState;
import sdp.world.Pitch;
import sdp.world.StaticWorldState;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.WorldState;

public class BallRecogniser implements ObjectRecogniser {
	private Pitch pitch;
	private Vision vision;
	private WorldState worldState;
	private PitchConstants pitchConstants;
	private DistortionFix distortionFix;
	private Vector2f previousBallPosition = new Vector2f(0, 0);
	private sdp.logging.Logging logger;

	public BallRecogniser(Vision vision, WorldState worldState,
			PitchConstants pitchConstants, DistortionFix distortionFix,
			Pitch pitch) {
		this.pitch = pitch;
		this.vision = vision;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.distortionFix = distortionFix;
		logger = new sdp.logging.Logging();
	}

	@Override
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
			Graphics2D debugGraphics, BufferedImage debugOverlay,
			StaticWorldState result) {
		ArrayList<Position> ballPoints = new ArrayList<Position>();
		int top = this.pitchConstants.getPitchTop();
		int left = this.pitchConstants.getPitchLeft();
		int right = left + this.pitchConstants.getPitchWidth();
		int bottom = top + this.pitchConstants.getPitchHeight();

		for (int row = top; row < bottom; row++) {
			for (int column = left; column < right; column++) {
				if (pixels[column][row] != null) {
					if (vision.isColour(pixels[column][row],
							PitchConstants.OBJECT_BALL)) {
						ballPoints.add(new Position(column, row));
						if (this.pitchConstants
								.debugMode(PitchConstants.OBJECT_BALL)) {
							debugOverlay.setRGB(column, row, 0xFF000000);
						}
					}
				}
			}
		}

		Vector2f ballPosition = vision.calculatePosition(ballPoints);
		
		if (ballPosition.x != 0 || ballPosition.y != 0) {
			debugGraphics.setColor(Color.red);
			debugGraphics.drawLine(0, (int) ballPosition.y, 640,
					(int) ballPosition.y);
			debugGraphics.drawLine((int) ballPosition.x, 0, (int) ballPosition.x,
					480);
		}

		worldState.ballNotOnPitch = false;
		
		if (ballPosition.x == 0 && ballPosition.y == 0) {
			ballPosition = previousBallPosition;
			worldState.ballNotOnPitch = true;
			// logger.Log("Ball Lost");
		} else {
			// Distortion fixing
			Point2D.Double point = new Point2D.Double(ballPosition.x,
					ballPosition.y);
			distortionFix.barrelCorrect(point);
			ballPosition.x = (float) point.x;
			ballPosition.y = (float) point.y;

			previousBallPosition = ballPosition;

			// logger.Log("X="+ballPosition.x+" Y="+ballPosition.y);
			// logger.Log("["+ballPosition.x+", "+ballPosition.y+"]");
		}
		
		MovingObject ball_m = new MovingObject(ballPosition.x, ballPosition.y);
		worldState.setBall(ball_m);
		
		//get prediction and compare #debugging
		//MovingObject ball_predicted_position = worldState.predictNextState(1);
		//float diff_x = ball_m.x - ball_predicted_position.x;
		//float diff_y = ball_m.y - ball_predicted_position.y;
		//System.out.println("diff x = "+diff_x+" | diff y = "+ diff_y);
		//System.out.println("X0="+ball_m.x);
		//System.out.println("X1="+ball_predicted_position.x);
		//update ball history
		//worldState.updateBallPositionHistory(ball_m);

		Point2D position = new Point2D.Double(ballPosition.x, ballPosition.y);
		pitch.framePointToModel(position);
		result.setBall(new Point((int) position.getX(), (int) position.getY()));
	}

	public static class ViewProvider implements PitchViewProvider {
		private DynamicWorldState dynamicWorldState;
		private Pitch pitch;
		
		public ViewProvider(DynamicWorldState dynamicWorldState, Pitch pitch) {
			this.dynamicWorldState = dynamicWorldState;
			this.pitch = pitch;
		}
		
		@Override
		public void drawOnPitch(Graphics2D g) {
			Shape ball = dynamicWorldState.getBall().getShape();
			if (ball != null) {
				g.setColor(Color.RED);
				g.fill(ball);
			}
		}
	}
}
