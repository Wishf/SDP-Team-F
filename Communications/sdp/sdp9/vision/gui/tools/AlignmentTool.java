package sdp.sdp9.vision.gui.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import sdp.sdp9.vision.PixelInfo;
import sdp.sdp9.vision.gui.GUITool;
import sdp.sdp9.vision.gui.VisionGUI;
import sdp.sdp9.vision.interfaces.ObjectRecogniser;
import sdp.sdp9.world.StaticWorldState;

public class AlignmentTool implements GUITool {
	private VisionGUI gui;
	private Point point = null;

	private final MouseAdapter mouseListener = new MouseAdapter() {
		@Override
		public void mouseMoved(MouseEvent e) {
			point = e.getPoint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			point = null;
		}
	};

	public AlignmentTool(VisionGUI gui) {
		this.gui = gui;
	}

	@Override
	public void activate() {
		gui.getVideoDisplay().addMouseListener(mouseListener);
		gui.getVideoDisplay().addMouseMotionListener(mouseListener);
	}

	@Override
	public boolean deactivate() {
		gui.getVideoDisplay().removeMouseListener(mouseListener);
		gui.getVideoDisplay().removeMouseMotionListener(mouseListener);
		point = null;
		return true;
	}

	@Override
	public void dispose() {
	}

	public class FrameDisplay implements ObjectRecogniser {

		@Override
		public void processFrame(PixelInfo[][] pixels, BufferedImage frame,
				Graphics2D debugGraphics, BufferedImage debugOverlay,
				StaticWorldState result) {
			if (point != null) {
				debugGraphics.setColor(Color.DARK_GRAY);
				debugGraphics.drawLine(point.x, 0, point.x,
						gui.getVideoHeight());
				debugGraphics
						.drawLine(0, point.y, gui.getVideoWidth(), point.y);

				debugGraphics.setColor(Color.WHITE);
				debugGraphics.drawString("x=" + point.x + "  y=" + point.y,
						300, 20);
			}
		}
	}
}
