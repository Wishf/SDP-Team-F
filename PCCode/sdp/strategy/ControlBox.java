package sdp.strategy;


import sdp.strategy.*;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;

public class ControlBox implements WorldStateControlBox {

    public static ControlBox controlBox = new ControlBox();

    private boolean isDefenderReady = false;
    private int curr_Y = -1;
    private boolean orth = false;
    private ControlBox(){}
    //If it has already been computed and still works, just return the value.
    //If not, compute a new one based on the position of the enemies attacker.
    public int getOrthogonal(WorldState ws){
        if(orth && passable(curr_X)){
            return curr_X;
        }
        orth = true;
        float defY = ws.getDefenderRobot().y;
        float oppY = ws.getEnemyAttackerRobot().y;
        float attY = ws.getAttackerRobot().y;
        float pitchY = ws.getPitch().getPitchHeight();
        if(oppY > pitchY/2.0){
            return (int)(oppY/2);
        } else {
            return (int)((pitchY-oppY)/2);
        }

    }

    public void avoidObstacle(boolean shouldAvoidObstacle) {

    };


    public void computePositions() {

    };


    public boolean shouldAttackerMove() {
        return false;
    }
    public Point2 AttackerPosition() {
        return new Point2(0,0);
    }


    public Point2 DefenderPosition() {
        return new Point2(0,0);
    }

    /*
       Once the defender is in the X position and ready to catch, it should tell the attacker to kick.
     */
    public void DefenderIsReady() {
        isDefenderReady = true;
    }
    public boolean isDefenderReady() {
        return isDefenderReady;
    }

    //Will return if the line to pass is empty, currently just returns false, so pass trajectory gets to be recomputed each time.
    public boolean passable(int y){
        return false;
    }

    //Returns true if robots can pass in current situation, currently returns true if diff between Y values is less than 100 and obstacle is more than 100 away.
    public boolean canPass(){
        float defY = ws.getDefenderRobot().y;
        float oppY = ws.getEnemyAttackerRobot().y;
        float attY = ws.getAttackerRobot().y;
        return Math.abs(defY-attY) < 100 && Math.abs(defY - oppY) > 100 && Math.abs(attY - oppY) > 100;
    }

}