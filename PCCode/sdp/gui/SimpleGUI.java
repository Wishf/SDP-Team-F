package sdp.gui;

import sdp.comms.Radio;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by conrad on 21/01/15.
 */
public class SimpleGUI extends JFrame implements ActionListener {

    private ArduinoWrapper arduino;
    private JPanel panel;
    private JTextArea debugInfo;

    public static void main(String args[]) {
        Radio.getPortNames();
        ArduinoWrapper arduino = new ArduinoWrapper();
        new SimpleGUI(arduino);

    }

    public SimpleGUI(ArduinoWrapper arduino) {
        this.arduino = arduino;
        arduino.start();

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        add(panel);

        String[] buttons = { "50cm Forward", "10cm Forward", "20cm Backward", "Kick", "Activate", "Deactivate", "Dance Like a Wee Lassie", "Start", "Stop", "Quit" };
        for(String button : buttons) {
            addButton(button, button);
        }

        JSlider kickPower = new JSlider(JSlider.HORIZONTAL,0,255,32);
        kickPower.setMajorTickSpacing(64);
        kickPower.setMinorTickSpacing(32);
        kickPower.setPaintTicks(true);
        kickPower.setPaintLabels(true);
        kickPower.setValue(255);
        kickPower.addChangeListener(new SliderChangeListener());
        panel.add(kickPower);

        SingletonDebugWindow debugWindow = new SingletonDebugWindow();
        JScrollPane spane = new JScrollPane(debugWindow.getTextArea());
        panel.add(spane);


        setSize(new Dimension(400, 400));
        setTitle("SDP Group 1 // Simple GUI");
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void addButton(String name, String command) {
        JButton button = new JButton(name);
        button.addActionListener(this);
        button.setActionCommand(command);
        panel.add(button);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        String command = actionEvent.getActionCommand();
        arduino.sendCommand(command);
    }

    private class SliderChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent changeEvent) {
            JSlider source = (JSlider)changeEvent.getSource();
            if (!source.getValueIsAdjusting()) {
                arduino.setKickPower((int)source.getValue());
            }
        }
    }
}
