package sdp.vision.interfaces;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import sdp.vision.PixelInfo;
import sdp.world.StaticWorldState;

public interface ObjectRecogniser {
	/* debugOverlay should not be needed in the future */
	/* Hope to remove the need for frame and only have the pixel info in the future - Peter*/
	public void processFrame(PixelInfo[][] pixels, BufferedImage frame, Graphics2D debugGraphics,
			BufferedImage debugOverlay, StaticWorldState result);
}
