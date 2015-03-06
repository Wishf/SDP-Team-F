package sdp.vision.gui.tools;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;

import sdp.vision.DistortionFix;
import sdp.vision.PitchConstants;
import sdp.vision.PixelInfo;
import sdp.vision.Position;
import sdp.vision.VideoStream;
import sdp.vision.YAMLConfig;
import sdp.vision.gui.GUITool;
import sdp.vision.gui.VisionGUI;
import sdp.vision.gui.VisionSettingsPanel;
import sdp.vision.interfaces.ObjectRecogniser;
import sdp.world.StaticWorldState;
import sdp.world.oldmodel.WorldState;

public class ColourThresholdConfigTool implements GUITool {
	private VisionGUI gui;
	private JFrame subWindow;

	private WorldState worldState;
	private PitchConstants pitchConstants;
	private VideoStream videoStream;
	private DistortionFix distortionFix;

	private VisionSettingsPanel settingsPanel;

	private final int videoWidth, videoHeight;

	// Pitch dimension selector variables
	private boolean selectionActive = false;
	private Point anchor;
	private int a;
	private int b;
	private int c;
	private int d;
	private int currentDivider;
	private int currentLeftGoal;
	private int currentRightGoal;
	private int currentTopBottom;
	private int currentPitchOutline;

	// Mouse listener variables
	boolean letterAdjustment = false;
	boolean yellowPlateAdjustment = false;
	boolean bluePlateAdjustment = false;
	boolean targetAdjustment = false;
	int mouseX;
	int mouseY;

	private MouseInputAdapter mouseSelector = new MouseInputAdapter() {
		Rectangle selection;

		public void mousePressed(MouseEvent e) {
			switch (settingsPanel.getMouseMode()) {
			case VisionSettingsPanel.MOUSE_MODE_OFF:
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:

				selectionActive = true;
				//System.out.println("Initialised anchor");
				// Pitch dimension selector
				anchor = e.getPoint();
				//System.out.println(anchor.x);
				//System.out.println(anchor.y);
				this.selection = new Rectangle(anchor);
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_TOP_BOTTOM:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_LEFT_GOAL:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_RIGHT_GOAL:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			}
		}

		public void mouseDragged(MouseEvent e) {
			switch (settingsPanel.getMouseMode()) {
			case VisionSettingsPanel.MOUSE_MODE_OFF:
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
				this.selection.setBounds((int) Math.min(anchor.x, e.getX()),
						(int) Math.min(anchor.y, e.getY()),
						(int) Math.abs(e.getX() - anchor.x),
						(int) Math.abs(e.getY() - anchor.y));
				a = (int) Math.min(anchor.x, e.getX());
				b = (int) Math.min(anchor.y, e.getY());
				c = (int) Math.abs(e.getX() - anchor.x);
				d = (int) Math.abs(e.getY() - anchor.y);
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_TOP_BOTTOM:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_LEFT_GOAL:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_RIGHT_GOAL:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
				mouseX = e.getX();
				mouseY = e.getY();
				break;
			}
		}

		public void mouseReleased(MouseEvent e) {

			switch (settingsPanel.getMouseMode()) {
			case VisionSettingsPanel.MOUSE_MODE_OFF:
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY:
				selectionActive = false;

				if (e.getPoint().distance(anchor) > 5) {
					Object[] options = { "Main Pitch", "Side Pitch", "Cancel" };
					int pitchNum = JOptionPane.showOptionDialog(gui,
							"The parameters are to be set for this pitch",
							"Picking a pitch",
							JOptionPane.YES_NO_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options,
							options[0]);

					// If option wasn't Cancel and the dialog wasn't closed
					if (pitchNum != 2 && pitchNum != JOptionPane.CLOSED_OPTION) {
						//System.out.println(pitchNum);
						int top = b;
						int bottom = b + d;
						int left = a;
						int right = a + c;

						if ((0 <= left && left < right && right < videoWidth)
								&& (0 <= top && top < bottom && bottom < videoHeight)) {
							// Update pitch constants
							pitchConstants.setPitchBounds(new Rectangle(a, b,
									c, d));
							pitchConstants.saveConstants(pitchNum);
						} else {
							//System.out.println("Pitch selection NOT succesful");
						}
					} else if (pitchNum == JOptionPane.CLOSED_OPTION
							|| pitchNum == 2) {
						//System.out.println("Closed option picked");
						a = pitchConstants.getPitchLeft();
						b = pitchConstants.getPitchTop();
						c = pitchConstants.getPitchWidth();
						d = pitchConstants.getPitchHeight();
					}
				}
				break;
			case VisionSettingsPanel.MOUSE_MODE_PITCH_TOP_BOTTOM:
				//System.out.println("Pitch outline selection.");
				pitchConstants.getPitchOutline()[currentPitchOutline] = new Position(e.getX(),e.getY());
				currentPitchOutline = (currentPitchOutline + 1) % 8;
				break;
			case VisionSettingsPanel.MOUSE_MODE_LEFT_GOAL:
				//System.out.println("Left goal selection mode");
				pitchConstants.getLeftGoal()[currentLeftGoal] = e.getY();
				currentLeftGoal = (currentLeftGoal + 1) % 3;
				break;
			case VisionSettingsPanel.MOUSE_MODE_RIGHT_GOAL:
				//System.out.println("Right goal selection mode");

				pitchConstants.getRightGoal()[currentRightGoal] = e.getY();
				currentRightGoal = (currentRightGoal + 1) % 3;
				break;
			case VisionSettingsPanel.MOUSE_MODE_DIVISIONS:
				//System.out.println("Division selection mode");
				pitchConstants.getDividers()[currentDivider] = e.getX();
				currentDivider = (currentDivider + 1) % 3;
				break;
			}
		}
	};

	public ColourThresholdConfigTool(VisionGUI gui, WorldState worldState,
			PitchConstants pitchConstants, VideoStream vStream,
			DistortionFix distortionFix, YAMLConfig yamlConfig) {
		this.gui = gui;
		this.worldState = worldState;
		this.pitchConstants = pitchConstants;
		this.videoStream = vStream;
		this.distortionFix = distortionFix;

		videoWidth = gui.getVideoWidth();
		videoHeight = gui.getVideoHeight();

		a = pitchConstants.getPitchLeft();
		b = pitchConstants.getPitchTop();
		c = pitchConstants.getPitchWidth();
		d = pitchConstants.getPitchHeight();

		subWindow = new JFrame("Settings");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		settingsPanel = new VisionSettingsPanel(worldState, pitchConstants,
				vStream, distortionFix, yamlConfig);
		subWindow.add(settingsPanel);

	}

	@Override
	public void activate() {

		gui.getVideoDisplay().addMouseListener(mouseSelector);
		gui.getVideoDisplay().addMouseMotionListener(mouseSelector);

		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x + mainWindowBounds.width,
				mainWindowBounds.y);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		gui.getVideoDisplay().removeMouseListener(mouseSelector);
		gui.getVideoDisplay().removeMouseMotionListener(mouseSelector);

		subWindow.setVisible(false);
		return true;
	}

	public class PitchBoundsDebugDisplay implements ObjectRecogniser {

		@Override
		public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
				Graphics2D debugGraphics, BufferedImage debugOverlay,
				StaticWorldState staticWorldState) {
			// Eliminating area around the pitch dimensions
			if (!selectionActive) {
				int left = pitchConstants.getPitchLeft();
				int top = pitchConstants.getPitchTop();
				int right = left + pitchConstants.getPitchWidth();
				int bottom = top + pitchConstants.getPitchHeight();
				// Making the pitch surroundings transparent
				Composite originalComposite = debugGraphics.getComposite();
				int type = AlphaComposite.SRC_OVER;
				AlphaComposite alphaComp = (AlphaComposite.getInstance(type,
						0.6f));
				debugGraphics.setComposite(alphaComp);
				debugGraphics.setColor(Color.BLACK);
				// Rectangle covering the TOP
				debugGraphics.fillRect(0, 0, videoWidth, top);
				// Rectangle covering the LEFT
				debugGraphics.fillRect(0, top, left, bottom - top);
				// Rectangle covering the BOTTOM
				debugGraphics.fillRect(0, bottom, videoWidth, videoHeight
						- bottom);
				// Rectangle covering the RIGHT
				debugGraphics.fillRect(right, top, videoWidth - right, bottom
						- top);
				// Setting back normal settings
				debugGraphics.setComposite(originalComposite);
			}

			if (settingsPanel.getMouseMode() == VisionSettingsPanel.MOUSE_MODE_PITCH_BOUNDARY) {
				// Draw the line around the pitch dimensions
				if (selectionActive) {
					debugGraphics.setColor(Color.YELLOW);
					debugGraphics.drawRect(a, b, c, d);
				}
			}
		}

	}

	public class DividerLineDebugDisplay implements ObjectRecogniser {

		@Override
		public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
				Graphics2D debugGraphics, BufferedImage debugOverlay,
				StaticWorldState staticWorldState) {
			// Drawing the dividing lines
			int[] ds = pitchConstants.getDividers();
			int top = pitchConstants.getPitchTop();
			int bot = top + pitchConstants.getPitchHeight();
			debugGraphics.setColor(Color.BLACK);
			debugGraphics.drawLine(ds[0], bot, ds[0], top);
			debugGraphics.drawLine(ds[1], bot, ds[1], top);
			debugGraphics.drawLine(ds[2], bot, ds[2], top);
			
			// Drawing the pitch outline
			Position[] out = pitchConstants.getPitchOutline();
			debugGraphics.setColor(Color.WHITE);
			for (int i = 0; i < 8; i++){
				// mod 8's so that the final line gets drawn back to the initial point.
				debugGraphics.drawLine(out[i].getX(), out[i].getY(), 
						out[(i+1)%8].getX(), out[(i+1)%8].getY());
			}

			// Draw labels
			debugGraphics.drawString("1", ds[0], bot + 20);
			debugGraphics.drawString("2", ds[1], bot + 20);
			debugGraphics.drawString("3", ds[2], bot + 20);
		}

	}

	public class GoalPositionDebugDisplay implements ObjectRecogniser {

		@Override
		public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
				Graphics2D debugGraphics, BufferedImage debugOverlay,
				StaticWorldState staticWorldState) {
			float[] lg = pitchConstants.getLeftGoal();
			float[] rg = pitchConstants.getRightGoal();
			int left = pitchConstants.getPitchLeft();
			int right = left + pitchConstants.getPitchWidth();
			debugGraphics.setColor(Color.WHITE);
			debugGraphics.drawRect(left - 5, (int) lg[0], 4, 4);
			debugGraphics.drawRect(left - 5, (int) lg[1], 4, 4);
			debugGraphics.drawRect(left - 5, (int) lg[2], 4, 4);
			debugGraphics.drawRect(right, (int) rg[0], 4, 4);
			debugGraphics.drawRect(right, (int) rg[1], 4, 4);
			debugGraphics.drawRect(right, (int) rg[2], 4, 4);
		}

	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}

}
