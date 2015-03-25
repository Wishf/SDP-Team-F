package sdp.vision;

import java.awt.Color;
import java.awt.image.BufferedImage;



public class NormalizationProcessor {
	static int COLOR_THRESH = 150;
	public static void normalize(BufferedImage frame){
		int h = frame.getHeight();
		int w = frame.getWidth();
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++){
				for(int j = 0; j < 32; j++){
					int p = 5;
				}
				int rgb = frame.getRGB(x, y);
				Color c = new Color(rgb);
				int r = (int)(c.getRed());
				int b = (int)(c.getBlue());
				int g = (int)(c.getGreen());
				//Discard pixels outside of field
				Position[] pos = PitchConstants.getPitchOutline(); 
				//Discard pixels which are similar to white or black
				//Discard pixels below
				
				//Filter or normalize the rest
				
				
				frame.setRGB(x, y, new Color(r, g, b).getRGB());
			}
		}
		//return null;
	}
}
