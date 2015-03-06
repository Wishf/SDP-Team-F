package sdp.comms;

import sdp.comms.packets.AttackerPacket;
import sdp.comms.packets.DefenderPacket;
import sdp.comms.packets.Packet;
import sdp.comms.packets.PositionPacket;
//import sun.nio.cs.Surrogate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/*
 * 
 * For two-robot setup:
 * 		Rewrite the radio singleton so it is a "doubleton"
 * 		Rewrite BrickCommServer so it differentiates between the attacker and defender 
 */
public class BrickCommServer implements PacketListener {
    Radio comm;
    private boolean connected;
    //This is set once the connection is established and it receives a packet from the Arduino.
    private Packet playerType;

    private ExecutorService executor;
    private List<StateChangeListener> stateChangeListeners;

    private final static ThreadFactory EXECUTOR_FACTORY = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread t = new Thread(runnable, "BrickCommServer executor");
            t.setDaemon(true);
            return t;
        }
    };

    public BrickCommServer(String name) {
        stateChangeListeners = new ArrayList<BrickCommServer.StateChangeListener>();
        connected = false;
        executor = Executors.newSingleThreadExecutor(EXECUTOR_FACTORY);
        //comm = new Radio("COM3");
        String[] serialPorts = Radio.getPortNames();
        
        //Now get an input from the user as to which port they want to use.
        System.out.println("Pick the connection for " + name + " port: ");
        Scanner userChoiceInput = new Scanner(System.in);
        int portNum = userChoiceInput.nextInt();
        while (!(portNum >= 0 && portNum < serialPorts.length)) {
            System.out.println("ERROR: You need to pick a number between 0 and " + serialPorts.length);
            System.out.println("Enter the number for the port you want to use: ");
            portNum = userChoiceInput.nextInt();
        }
        comm = new SingletonRadio(serialPorts[portNum]);
        comm.addListener(this);
        PositionPacket whatPosition = new PositionPacket();
        comm.sendPacket(whatPosition);
    }

    public void connect() {
        System.out.print("Connecting");
        //comm.start();
        setConnected(true);
    }

    public boolean isConnected() {
        return connected;
    }
    public boolean isAttacker() { return playerType instanceof AttackerPacket; }
    public boolean isIdentified() { return playerType != null; }

    private void setConnected(boolean connected) {
        if (this.connected != connected) {
            this.connected = connected;

            for (StateChangeListener listener : stateChangeListeners) {
                listener.stateChanged();
            }
        }
    }

    public void close() {
        if (comm != null)
            comm.stop();


        setConnected(false);
    }

    public void addStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.add(listener);
    }

    public void removeStateChangeListener(StateChangeListener listener) {
        stateChangeListeners.remove(listener);
    }

    /**
     * Executes a command asynchronously. Returns immediately and is safe to
     * call from any thread.
     */
    public void execute(final RobotCommand.Command command) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                BrickCommServer.this.executeSync(command);
            }
        });
    }

    /**
     * Executes a command synchronously. Never call this method from GUI or
     * frame grabber thread!
     */
    public void executeSync(RobotCommand.Command command) {
        try {
            command.sendToBrick(comm);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    @Override
    public void packetArrived(Packet p) {
        if(p instanceof AttackerPacket || p instanceof DefenderPacket){
            this.playerType = p;
        }
    }
    

    public interface StateChangeListener {
        void stateChanged();
    }
}
