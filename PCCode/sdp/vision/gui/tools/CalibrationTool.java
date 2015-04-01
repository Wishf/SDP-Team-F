package sdp.vision.gui.tools;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import sdp.control.HolonomicRobotController;
import sdp.strategy.StrategyController;
import sdp.vision.gui.GUITool;
import sdp.vision.gui.VisionGUI;

public class CalibrationTool implements GUITool, ActionListener {
	protected VisionGUI gui;
	protected JFrame subWindow;
	protected StrategyController sc;
	protected JButton attOpenButton, attSaveButton, defOpenButton, defSaveButton;
	protected JLabel attFileLabel, defFileLabel;
    protected JFileChooser fc;
    protected JButton kickIncreaseButton, kickDecreaseButoon, calibrateCatcherButton;
	
	
	ArrayList<MappedJSlider> sliders;

	
	public CalibrationTool(VisionGUI gui, StrategyController sc) {
		this.gui = gui;
		this.sc = sc;
		
		
		//Create a file chooser
		this.fc = new JFileChooser();
		
		
		
		sliders = new ArrayList<MappedJSlider>();
		
		UIManager.put("Slider.paintValue", false);

		subWindow = new JFrame("Calibration");
		subWindow.setResizable(false);
		subWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		
		
		
		JTabbedPane tabbedPane = new JTabbedPane();
		subWindow.getContentPane().add(tabbedPane);
		
		
		JPanel defenderTab = new JPanel();
        JPanel attackerTab = new JPanel();
        
        defenderTab.setLayout(new GridLayout(0, 3));
        attackerTab.setLayout(new GridLayout(0, 3));
        
        
        
        tabbedPane.addTab("Defender", defenderTab);
        tabbedPane.addTab("Attacker", attackerTab);
        
        createSaveLoadButtons(attackerTab, defenderTab);
        
        //Defender
        createCalibButtons(defenderTab, sc.bcsDefender.robotController);  
        addAllSliders(defenderTab, sc.bcsDefender.robotController);   
        //Attacker
        createCalibButtons(attackerTab, sc.bcsAttacker.robotController);
        addAllSliders(attackerTab, sc.bcsAttacker.robotController);
		
	}
	
	
	private void createCalibButtons(JPanel container, final HolonomicRobotController rc){
		kickIncreaseButton = new JButton("Wind up kicker");
		kickDecreaseButoon = new JButton("Wind down buton");
		calibrateCatcherButton = new JButton("Calibrate Catcher");
		
		
		
		kickIncreaseButton.addActionListener(new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		        rc.increaseKicker();
		    }
		});
		kickDecreaseButoon.addActionListener(new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		        rc.decreaseKicker();
		    }
		});
		calibrateCatcherButton.addActionListener(new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		        rc.calibrateCatcher();
		    }
		});
		
		container.add(kickIncreaseButton);
		container.add(kickDecreaseButoon);
		container.add(calibrateCatcherButton);
	}
	
	
	private void createSaveLoadButtons(JPanel attCont, JPanel defCont){
		//Attacker
		attOpenButton = new JButton("Load from a file");
		attOpenButton.addActionListener(this);
	
		attSaveButton = new JButton("Save to a file");
		attSaveButton.addActionListener(this);
		
		attFileLabel = new JLabel("none");
		
		
		attCont.add(attOpenButton);
		attCont.add(attSaveButton);
		attCont.add(attFileLabel);
		
		
		//Defender
		defOpenButton = new JButton("Load from a file");
		defOpenButton.addActionListener(this);
	
		defSaveButton = new JButton("Save to a file");
		defSaveButton.addActionListener(this);
		
		defFileLabel = new JLabel("none");
		
		
		defCont.add(defOpenButton);
		defCont.add(defSaveButton);
		defCont.add(defFileLabel);
	}
	
	private void addAllSliders(JPanel container, HolonomicRobotController rc){
		addSlider(container, "ROTATE_MIN", 20, 150, 1, rc);
        addSlider(container, "ROTATE_MAX", 150, 254, 1, rc);
        addSlider(container, "ROTATE_A", 0, 20000, 0.0001, rc);
        addSlider(container, "ROTATE_C", 0, 200, 1, rc);
        addSlider(container, "ROTATE_REAR_COEF", 0, 200, 0.01, rc);
        
        addSlider(container, "TRAVEL_MIN", 50, 150, 1, rc);
        addSlider(container, "TRAVEL_MAX", 150, 254, 1, rc);
        addSlider(container, "TRAVEL_A", 0, 5000, 0.001, rc);
        addSlider(container, "TRAVEL_C", 0, 200, 1, rc);
        
        addSlider(container, "SIDEWAYS_MIN", 50, 254, 1, rc);
        addSlider(container, "SIDEWAYS_MAX", 50, 254, 1, rc);
        addSlider(container, "SIDEWAYS_A", 0, 5000, 0.001, rc);
        addSlider(container, "SIDEWAYS_C", 0, 200, 1, rc);
        addSlider(container, "SIDEWAYS_ARC_POWER", 0, 254, 1, rc);
	}
	
	
	private void addSlider(JPanel container, String name, int min, int max, double scaling, HolonomicRobotController rc){
		JLabel label = new JLabel(name+" = ");		
		JLabel valueLabel = new JLabel();		
		MappedJSlider slider = new MappedJSlider(name, JSlider.HORIZONTAL, min, max, scaling, rc, valueLabel);
		
		label.setHorizontalAlignment(JLabel.RIGHT);
		
		
		
		container.add(label);
		container.add(valueLabel);
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
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		String name;
		HolonomicRobotController rc;
		double scaling;
		private JLabel valueLabel;
		
		public MappedJSlider(String name, int orientation, int min, int max, double scaling, HolonomicRobotController rc, JLabel valueLabel){
			super(orientation, min, max, (int) (rc.getRCValue(name)/scaling));
			this.scaling = scaling;
			
			this.name = name;
			this.rc = rc;
			this.valueLabel = valueLabel;
			
			Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
			labelTable.put( new Integer( min ), new JLabel(""+min*scaling) );
			labelTable.put( new Integer( max ), new JLabel(""+max*scaling) );
			
			this.setLabelTable(labelTable);
			this.setPaintLabels(true);
			
			this.addChangeListener(this);
			
			setValueLabel();
		}		
		
		
		private void setValueLabel(){
			
			double power = Math.log10(scaling);
			double factor = Math.pow(10, -power);
			
			double value = (rc.getRCValue(name));
			
			double labelValue = (double)Math.round(value * factor) / factor;
			
			//System.out.println(factor+" "+value);
			
			this.valueLabel.setText(""+labelValue);
			
			
		}
		
		public void stateChanged(ChangeEvent e) {
		    JSlider source = (JSlider)e.getSource();
		    if ( true){ //!source.getValueIsAdjusting()) {
		        int value = (int)source.getValue();
		        
		        double actualValue = value*this.scaling;
		        
		        rc.setRCValue(this.name, actualValue);
		        
		        setValueLabel();
		    }
		}
		
		
		public void updateValue(){
			this.setValue((int) (rc.getRCValue(name)/scaling));
		}
		
		
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		updateAllSliders();
		//Handle attacker open button action.
        if (e.getSource() == attOpenButton) {
            int returnVal = fc.showOpenDialog(subWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                System.out.println(System.getProperty("user.dir"));
                attFileLabel.setText(file.getName());
                
                
                
                if(!sc.bcsAttacker.robotController.loadConfig(file)){
                	System.out.println("Problem loading!");
                }
                
            } else {
            	System.out.println("Open command cancelled by user.");
            }
        //Handle attacker save button action.
        } else if (e.getSource() == attSaveButton) {
            int returnVal = fc.showSaveDialog(subWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                //System.out.println("Att Opening: " + file.getName());
                attFileLabel.setText(file.getName());
                
                sc.bcsAttacker.robotController.saveConfig(file);
                
                
            } else {
            	System.out.println("Open command cancelled by user.");
            }
        }
        
        //Handle defender open
        else if (e.getSource() == defOpenButton) {
            int returnVal = fc.showOpenDialog(subWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                
                //System.out.println("Def Opening: " + file.getName());
                defFileLabel.setText(file.getName());
                
                if(!sc.bcsDefender.robotController.loadConfig(file)){
                	System.out.println("Problem loading!");
                }
                
            } else {
            	System.out.println("Open command cancelled by user.");
            }
        //Handle defender save
        } else if (e.getSource() == defSaveButton) {
            int returnVal = fc.showSaveDialog(subWindow);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                
                //System.out.println("Def Saving: " + file.getName());
                defFileLabel.setText(file.getName());
                
                
                
                sc.bcsDefender.robotController.saveConfig(file);
            } else {
            	System.out.println("Open command cancelled by user.");
            }
        }
        
        updateAllSliders();
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



