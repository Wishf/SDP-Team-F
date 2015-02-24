package sdp.comms;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import sdp.comms.packets.*;
import sdp.gui.SingletonDebugWindow;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Matthew on 16/01/2015.
 */
public class Radio {
    private SerialPort port;
    private Queue<Packet> packetQueue;
    private List<PacketListener> listenerList;

    public Radio(String portName){
    	if(portName != null) {
    		port = new SerialPort(portName);
            packetQueue = new LinkedList<Packet>();
            listenerList = new LinkedList<PacketListener>();
    	}
    }

    public static void getPortNames() {
        SingletonDebugWindow debugWindow = new SingletonDebugWindow();
        for (String s : SerialPortList.getPortNames()) {
            debugWindow.addDebugInfo(s);
        }
    }

    public void start(){
        try {
            port.openPort();
            port.setParams(SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            port.setEventsMask(SerialPort.MASK_RXCHAR);
            port.addEventListener(new RadioController(packetQueue, port, listenerList));
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
        packetQueue.add(packet);
        Packet next = packetQueue.poll();
        if(next != null) {
            System.out.println("Sent packet");
            try {
            	System.out.println(packet);
                next.writePacket(port);
            } catch (SerialPortException e) {
                e.printStackTrace();
            }
        }
    }

    public void addListener(PacketListener listener) {
        listenerList.add(listener);
    }
}
