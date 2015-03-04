package sdp.comms;

import java.awt.Dialog.ModalityType;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

@SuppressWarnings("serial")
public class BrickControlGUI extends JFrame implements KeyListener {
	private BrickCommServer brick;

	public BrickControlGUI(final BrickCommServer brick) {
		this.brick = brick;

		setTitle("Robot controller");
		setSize(400, 200);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				brick.close();
			}
		});

		JLabel label = new JLabel("Use arrow keys to control the robot.",
				JLabel.CENTER);
		label.setFocusable(true);
		add(label);

		label.addKeyListener(this);
	}

	@Override
	public void keyPressed(KeyEvent key) {
		RobotCommand.Command command = null;
		switch (key.getKeyCode()) {
		case KeyEvent.VK_UP:
			command = new RobotCommand.Travel(10000, 100);
			break;
		case KeyEvent.VK_DOWN:
			command = new RobotCommand.Travel(-10000, 100);
			break;
		case KeyEvent.VK_SPACE:
			command = new RobotCommand.Kick(30);
			break;
		case KeyEvent.VK_1:
			command = new RobotCommand.Catch();
			break;
		case KeyEvent.VK_LEFT:
			command = new RobotCommand.Rotate(-45, 45);
			break;
		case KeyEvent.VK_RIGHT:
			command = new RobotCommand.Rotate(45, 45);
			break;
		}
		if (command != null) {
			brick.execute(command);
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		brick.execute(new RobotCommand.Stop());
	}

	@Override
	public void keyTyped(KeyEvent key) {
	}

	public static void guiConnect(final BrickCommServer brick) {
		new GUIConnect(brick);
	}

	public static void main(String[] args) {
		BrickCommServer bcs = new BrickCommServer("yola");
		// BrickControlGUI.guiConnect(bcs, BtInfo.MEOW);
		BrickControlGUI client = new BrickControlGUI(bcs);
		client.setVisible(true);
	}

	private static class GUIConnect {
		private JDialog window;

		public GUIConnect(final BrickCommServer brick) {
			window = new JDialog(null, "Connecting",
					ModalityType.APPLICATION_MODAL);
			window.setSize(400, 150);
			window.setResizable(false);
			window.setLocationRelativeTo(null);
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			window.setVisible(true);
		}
	}
}
