package sdp.vision.gui.tools;

import java.awt.Container;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import sdp.strategy.StrategyController;
import sdp.vision.gui.GUITool;
import sdp.vision.gui.VisionGUI;

public class RobotDebugWindow implements GUITool {

	private VisionGUI gui;
	private JFrame subWindow;
	private StrategyController sc;
	private JLabel infoLabel = new JLabel();
	public static DebugWindow messageAttacker;
	public static DebugWindow messageDefender;

	public RobotDebugWindow(VisionGUI gui, StrategyController sc) {
		this.gui = gui;
		this.sc = sc;

		subWindow = new JFrame("Robot Debug Window");
		subWindow.setResizable(true);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		sc.addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				updateInfoLabel();
			}
		});
		infoLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);

		Container contentPane = subWindow.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		contentPane.add(infoLabel);
		if(messageAttacker == null) {
			messageAttacker = new DebugWindow("Attacker");
		}
		if(messageDefender == null) {
			messageDefender = new DebugWindow("Defender");
		}
		contentPane.add(messageAttacker);
		contentPane.add(messageDefender);
		//contentPane.add(new AdvancedStrategyEnabler());
		updateInfoLabel();
	}

	private void updateInfoLabel() {
		infoLabel.setText("<html><b>Messages from the robots: </b>");
	}

	@Override
	public void activate() {
		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x, mainWindowBounds.y
				+ mainWindowBounds.height);
		subWindow.pack();
		subWindow.setVisible(true);
	}

	@Override
	public boolean deactivate() {
		subWindow.setVisible(false);
		return true;
	}

	@Override
	public void dispose() {
		subWindow.dispose();
	}


	@SuppressWarnings("serial")
	public class DebugWindow extends JPanel {
		private JLabel label = new JLabel();
		private JTextField field = new JTextField(100);

		public DebugWindow(String robot) {
			this.add(label);
            this.add(field);
            this.label.setText(robot + ": ");
		}
		
		public void setMessage(String message) {
			field.setText(message);
		}

	}
	

}