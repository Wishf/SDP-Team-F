package sdp.vision.gui.tools;

import sdp.vision.gui.GUITool;
import sdp.vision.gui.VisionGUI;

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
