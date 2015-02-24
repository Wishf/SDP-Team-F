package sdp.sdp9.vision.gui.tools;

import sdp.sdp9.vision.gui.GUITool;
import sdp.sdp9.vision.gui.VisionGUI;

public class DefaultTool implements GUITool {
	private VisionGUI gui;

	public DefaultTool(VisionGUI gui) {
		this.gui = gui;
	}
	
	@Override
	public void activate() {
	}

	@Override
	public boolean deactivate() {
		return true;
	}

	@Override
	public void dispose() {
	}

}
