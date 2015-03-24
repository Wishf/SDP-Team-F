package sdp.gui;

import sdp.comms.Radio;
import sdp.comms.SingletonRadio;
import sdp.comms.packets.DisengageCatcherPacket;
import sdp.comms.packets.DrivePacket;
import sdp.comms.packets.EngageCatcherPacket;
import sdp.comms.packets.KickPacket;
import sdp.util.DriveDirection;

import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;

public class HardwareTester extends JDialog {
    private JPanel contentPane;
    private JButton kickButton;
    private JButton catchButton;
    private JButton uncatchButton;
    private JSlider slider1;
    private JButton forwardButton;
    private JButton backwardButton;
    private SingletonRadio radio;

    public HardwareTester() {
        setContentPane(contentPane);
        setModal(true);

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        String[] serialPorts = Radio.getPortNames();

        System.out.println("Please select radio port: ");
        Scanner userChoiceInput = new Scanner(System.in);
        int portNum = userChoiceInput.nextInt();
        while (!(portNum >= 0 && portNum < serialPorts.length)) {
            System.out.println("ERROR: You need to pick a number between 0 and " + serialPorts.length);
            System.out.println("Enter the number for the port you want to use: ");
            portNum = userChoiceInput.nextInt();
        }
        radio = new SingletonRadio(serialPorts[portNum]);

        kickButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radio.sendPacket(new KickPacket());
            }
        });
        catchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radio.sendPacket(new EngageCatcherPacket());
            }
        });
        uncatchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                radio.sendPacket(new DisengageCatcherPacket());
            }
        });
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte power = (byte) slider1.getValue();
                radio.sendPacket(new DrivePacket(power, DriveDirection.FORWARD,
                        power, DriveDirection.FORWARD, power, DriveDirection.FORWARD, 1000));
            }
        });
        backwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                byte power = (byte) slider1.getValue();
                radio.sendPacket(new DrivePacket(power, DriveDirection.BACKWARD,
                        power, DriveDirection.BACKWARD, power, DriveDirection.BACKWARD, 1000));
            }
        });
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        HardwareTester dialog = new HardwareTester();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
