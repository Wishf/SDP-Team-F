package sdp.gui;

import sdp.comms.packets.*;

/**
 * Created by conrad on 27/01/15.
 */
public class PacketLifeTime implements Runnable {
    private Thread thread;
    private int time;
    private Packet packet;
    public PacketLifeTime(Packet packet, int time) {
        this.time = time;
        this.packet = packet;
    }

    public void start() {
        if(thread == null) {
            thread = new Thread(this, "RandomPacket");
            thread.start();
        }
    }

    public void run() {
        try {
            thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SingletonRadio rad = new SingletonRadio("/dev/ttyACM0");
        System.out.println(packet);
        rad.sendPacket(packet);
    }
}
