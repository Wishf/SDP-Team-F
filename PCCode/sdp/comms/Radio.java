package sdp.comms;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import sdp.comms.packets.*;
import sdp.gui.SingletonDebugWindow;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Matthew on 16/01/2015.
 */
public class Radio {
    private SerialPort port;
    private String portName;
    private List<PacketListener> listenerList;

    public Radio(String portName){
    	if(portName != null) {
    		port = new SerialPort(portName);
    		this.portName = portName;
            listenerList = new LinkedList<PacketListener>();
    	}
    }

    public static String[] getPortNames() {
        SingletonDebugWindow debugWindow = new SingletonDebugWindow();
        debugWindow.addDebugInfo("Serial Ports available on this computer: ");
        String [] serialPorts = SerialPortList.getPortNames();
        for (int i=0; i<serialPorts.length; i++) {
            debugWindow.addDebugInfo("[" + i + "]  " + serialPorts[i]);
        }
        return serialPorts;
    }

    public void start(){
        try {
            port.openPort();
            port.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            port.setEventsMask(SerialPort.MASK_RXCHAR);
            port.addEventListener(new RadioController(port, listenerList));
        }
        catch(SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        try{
            port.closePort();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(Packet packet) {
        try {
        	////System.out.println(packet);
        	////System.out.println(portName);
            packet.writePacket(port);
            ////System.out.println("Sent packet");
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void addListener(PacketListener listener) {
        listenerList.add(listener);
    }
}
