package sdp.control;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Matthew on 06/02/2015.
 */
public class MotionQueue {
    private Queue<Maneuver> motions;
    private int size;

    public MotionQueue(int capacity) {
        size = capacity;
        motions = new LinkedList<Maneuver>();
    }

    public void pop(){
        motions.poll();
    }

    public void clear() {
        motions.clear();
    }

    public boolean enqueue(Maneuver maneuver) {
        if(motions.size() < size) {
            motions.add(maneuver);
            return true;
        }

        return false;
    }

    public boolean full()
    {
        return motions.size() == size;
    }
}
