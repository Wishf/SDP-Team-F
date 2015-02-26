package sdp.vision.gui.tools;

import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.strategy.StrategyController;
import sdp.strategy.StrategyController.StrategyType;
import sdp.vision.gui.GUITool;
import sdp.vision.gui.VisionGUI;

public class StrategySelectorTool implements GUITool {

	private VisionGUI gui;
	private JFrame subWindow;
	private StrategyController sc;
	private JLabel infoLabel = new JLabel();

	public StrategySelectorTool(VisionGUI gui, StrategyController sc) {
		this.gui = gui;
		this.sc = sc;

		subWindow = new JFrame("Strategy Selector");
		subWindow.setResizable(false);
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
//		contentPane.add(new ConnectionControl("Attacker", sc.bcsAttacker,
//				BtInfo.group10));
//		contentPane.add(new ConnectionControl("Defender", sc.bcsDefender,
//				BtInfo.MEOW));
//		contentPane.add(new ConnectionControl("Attacker", sc.bcsAttacker,
//				BtInfo.DemoD));
//		contentPane.add(new ConnectionControl("Defender", sc.bcsDefender,
//				BtInfo.DemoA));
		contentPane.add(infoLabel);
		contentPane.add(new StrategyPicker());
		contentPane.add(new AdvancedStrategyEnabler());
		updateInfoLabel();
	}

	private void updateInfoLabel() {
		infoLabel.setText("<html>Current strategy: <b>"
				+ sc.getCurrentStrategy() + "</b><br />Auto control: <b>"
				+ (sc.isPaused() ? "paused" : "running") + "</b></html>");
		subWindow.pack();
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
	private static class ConnectionControl extends JPanel implements
			BrickCommServer.StateChangeListener {
		private String role;
		private BrickCommServer bcs;

		private JLabel statusLabel;
		private JButton disconnectBtn;
		private JButton resetCatcherBtn;

		public ConnectionControl(String role, final BrickCommServer bcs) {
			this.role = role;
			this.bcs = bcs;
			bcs.addStateChangeListener(this);

			statusLabel = new JLabel();
			add(statusLabel);

			disconnectBtn = new JButton("Disconnect");
			disconnectBtn.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					bcs.close();
				}
			});
			add(disconnectBtn);
			
			resetCatcherBtn = new JButton("Reset catcher");
			resetCatcherBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent event) {
					bcs.execute(new RobotCommand.ResetCatcher());
				}
			});
			add(resetCatcherBtn);

			stateChanged();
		}

		@Override
		public void stateChanged() {
			disconnectBtn.setVisible(bcs.isConnected());
			resetCatcherBtn.setVisible(bcs.isConnected());
		}
	}

	@SuppressWarnings("serial")
	public class StrategyPicker extends JPanel {
		//private JButton atkStrat = new JButton("Attacking");
		//private JButton defStrat = new JButton("Defending");
		//private JButton passStrat = new JButton("Passing");
		//private JButton marStrat = new JButton("Marking");
		private JButton nullStrat = new JButton("Deactivate");
		private JButton actStrat = new JButton("Activate");
        private JButton mstAStrat = new JButton("Milestone 2 - Interceptor");
        private JButton mstBStrat = new JButton("Milestone 2 - Score");
        private JButton ms3AStrat = new JButton("MILESTONE 3 - Pass task 1");
        private JButton ms3BStrat = new JButton("MILESTONE 3 - Pass task 2");
		private JButton pauseController = new JButton("Pause");
		private JButton startController = new JButton("Start");

		public StrategyPicker() {
			//this.add(atkStrat);
			//this.add(defStrat);
			//this.add(passStrat);
			//this.add(marStrat);
			this.add(nullStrat);
			this.add(actStrat);
            this.add(mstAStrat);
            this.add(mstBStrat);
            this.add(ms3AStrat);
            this.add(ms3BStrat);
			this.add(pauseController);
			this.add(startController);

			/*atkStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.ATTACKING);
				}
			});
			defStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.DEFENDING);
				}
			});
			passStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.PASSING);
				}
			});
			marStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.MARKING);
				}
			});*/
			nullStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.DO_NOTHING);
				}
			});
			
			actStrat.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sc.changeToStrategy(StrategyType.DO_SOMETHING);
				}
			});
            //mstAStrat.addActionListener((e) -> { sc.changeToStrategy(StrategyType.MILESTONE_TWO_A); });
            //mstBStrat.addActionListener((e) -> { sc.changeToStrategy(StrategyType.MILESTONE_TWO_B); });
			
			mstAStrat.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					sc.changeToStrategy(StrategyType.MILESTONE_TWO_A);
				}
			});
			
			mstBStrat.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					sc.changeToStrategy(StrategyType.MILESTONE_TWO_B);
				}
			});

            ms3AStrat.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) { sc.changeToStrategy(StrategyType.MILESTONE_THREE_A);}
            });

            ms3BStrat.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) { sc.changeToStrategy(StrategyType.MILESTONE_THREE_B);}
            });

			pauseController.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					sc.setPaused(true);
				}
			});
			startController.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					sc.setPaused(false);
				}
			});
		}

	}
	
	@SuppressWarnings("serial")
	public class AdvancedStrategyEnabler extends JPanel{
		private JCheckBox confusionEnabled = new JCheckBox("Confuse Shot");
		private JCheckBox bounceShotEnabled = new JCheckBox("Bounce Shot");
		private JCheckBox interceptorDefence = new JCheckBox("Interceptor Defence");
		private JCheckBox bouncePassEnabled = new JCheckBox("Bounce Pass");
		
		public AdvancedStrategyEnabler(){
			this.add(confusionEnabled);
			this.add(bounceShotEnabled);
			this.add(interceptorDefence);
			this.add(bouncePassEnabled);
			
			confusionEnabled.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StrategyController.confusionEnabled = confusionEnabled.isSelected();
				}
			});
			
			bounceShotEnabled.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StrategyController.bounceShotEnabled = bounceShotEnabled.isSelected();
				}
			});
			
			interceptorDefence.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					StrategyController.interceptorDefenceEnabled = interceptorDefence.isSelected();
				}
			});
			
			bouncePassEnabled.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					StrategyController.bouncePassEnabled = bouncePassEnabled.isSelected();
				}
			});
		}
	}

}
