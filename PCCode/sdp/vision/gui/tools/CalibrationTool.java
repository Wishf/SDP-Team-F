package sdp.vision.gui.tools;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import sdp.control.HolonomicRobotController;
import sdp.strategy.StrategyController;
import sdp.vision.gui.GUITool;
import sdp.vision.gui.VisionGUI;

public class CalibrationTool implements GUITool {
	private VisionGUI gui;
	private JFrame subWindow;
	private StrategyController sc;
	
	
	ArrayList<MappedJSlider> sliders;

	
	public CalibrationTool(VisionGUI gui, StrategyController sc) {
		this.gui = gui;
		this.sc = sc;
		
		sliders = new ArrayList<MappedJSlider>();

		subWindow = new JFrame("Calibration");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		
		
		
		JTabbedPane tabbedPane = new JTabbedPane();
		subWindow.getContentPane().add(tabbedPane);
		
		
		JPanel defenderTab = new JPanel();
        JPanel attackerTab = new JPanel();
        
        defenderTab.setLayout(new GridLayout(0, 2));
        attackerTab.setLayout(new GridLayout(0, 2));
        
        
        
        tabbedPane.addTab("Defender", defenderTab);
        tabbedPane.addTab("Attacker", attackerTab);
		
        
        
        //Defender
        addSlider(defenderTab, "ROTATE_MIN", 0, 255, 1, sc.bcsDefender.robotController);
        addSlider(defenderTab, "ROTATE_MAX", 0, 255, 1, sc.bcsDefender.robotController);
        addSlider(defenderTab, "ROTATE_A", 0, 1000, 0.01, sc.bcsDefender.robotController);
        addSlider(defenderTab, "ROTATE_REAR_COEF", 0, 100, 0.01, sc.bcsDefender.robotController);
        addSlider(defenderTab, "ROTATE_INITIAL_BOOST_COEF", 100, 300, 0.01, sc.bcsDefender.robotController);
        
        addSlider(defenderTab, "TRAVEL_MIN", 0, 255, 1, sc.bcsDefender.robotController);
        addSlider(defenderTab, "TRAVEL_MAX", 0, 255, 1, sc.bcsDefender.robotController);
        addSlider(defenderTab, "TRAVEL_A", 0, 1000, 0.01, sc.bcsDefender.robotController);
        
        addSlider(defenderTab, "SIDEWAYS_MIN", 0, 255, 1, sc.bcsDefender.robotController);
        addSlider(defenderTab, "SIDEWAYS_MAX", 0, 255, 1, sc.bcsDefender.robotController);
        addSlider(defenderTab, "SIDEWAYS_A", 0, 1000, 0.01, sc.bcsDefender.robotController);
        addSlider(defenderTab, "SIDEWAYS_ARC_POWER", 0, 255, 1, sc.bcsDefender.robotController);
        
        
        
        //Attacker
        addSlider(attackerTab, "ROTATE_MIN", 0, 255, 1, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "ROTATE_MAX", 0, 255, 1, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "ROTATE_A", 0, 1000, 0.01, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "ROTATE_REAR_COEF", 0, 100, 0.01, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "ROTATE_INITIAL_BOOST_COEF", 100, 300, 0.01, sc.bcsAttacker.robotController);
        
        addSlider(attackerTab, "TRAVEL_MIN", 0, 255, 1, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "TRAVEL_MAX", 0, 255, 1, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "TRAVEL_A", 0, 1000, 0.01, sc.bcsAttacker.robotController);
        
        addSlider(attackerTab, "SIDEWAYS_MIN", 0, 255, 1, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "SIDEWAYS_MAX", 0, 255, 1, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "SIDEWAYS_A", 0, 1000, 0.01, sc.bcsAttacker.robotController);
        addSlider(attackerTab, "SIDEWAYS_ARC_POWER", 0, 255, 1, sc.bcsAttacker.robotController);
		
	}
	
	private void addSlider(JPanel container, String name, int min, int max, double scaling, HolonomicRobotController rc){
		JLabel label = new JLabel(name);
		container.add(label);
		MappedJSlider slider = new MappedJSlider(name, JSlider.HORIZONTAL, min, max, scaling, rc);
		container.add(slider);
		
		sliders.add(slider);
	}
	
	public void updateAllSliders(){
		for(MappedJSlider slider:sliders){
			slider.updateValue();
		}
	}
	
	
	public void stateChanged(ChangeEvent e) {
		
	}
	
	
	
	private class MappedJSlider extends JSlider implements ChangeListener{
		
		String name;
		HolonomicRobotController rc;
		double scaling;
		
		public MappedJSlider(String name, int orientation, int min, int max, double scaling, HolonomicRobotController rc){
			super(orientation, min, max, (int) (rc.getRCValue(name)/scaling));
			this.scaling = scaling;
			
			this.name = name;
			this.rc = rc;
			
			Hashtable labelTable = new Hashtable();
			labelTable.put( new Integer( min ), new JLabel(""+min*scaling) );
			labelTable.put( new Integer( max ), new JLabel(""+max*scaling) );
			
			this.setLabelTable(labelTable);
			this.setPaintLabels(true);
			
			this.addChangeListener(this);
		}		
		
		

		
		public void stateChanged(ChangeEvent e) {
		    JSlider source = (JSlider)e.getSource();
		    if (!source.getValueIsAdjusting()) {
		        int value = (int)source.getValue();
		        
		        
		        rc.setRCValue(this.name, value);
		    }
		}
		
		
		public void updateValue(){
			this.setValue((int) (rc.getRCValue(name)/scaling));
		}
		
		
		
	}
	
	
	

	@Override
	public void activate() {
		Rectangle mainWindowBounds = gui.getBounds();
		subWindow.setLocation(mainWindowBounds.x, mainWindowBounds.y
				+ mainWindowBounds.height);
		subWindow.pack();
		subWindow.setVisible(true);
		
		updateAllSliders();
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
}



