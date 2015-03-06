package sdp.strategy;


import sdp.strategy.*;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;

public class ControlBox implements WorldStateControlBox {

    public static ControlBox controlBox = new ControlBox();
       
    private boolean avoid = true;
    private boolean isDefenderReady = true;
    private int curr_Y = -1;
    private boolean orth = true;
    private Point2 attPos = new Point2(0, 0);
    private Point2 defPos = new Point2(0, 0);
    private boolean comp = false;
    private ControlBox(){}
    //If it has already been computed and still works, just return the value.
    //If not, compute a new one based on the position of the enemies attacker.
    public int getOrthogonal(WorldState ws){
        float defY = ws.getDefenderRobot().y;
        float oppY = ws.getEnemyAttackerRobot().y;
        float attY = ws.getAttackerRobot().y;
        float pitchY = ws.getPitch().getPitchHeight();
        if(oppY > pitchY/2.0){
            return (int)(oppY/2);
        } else {
            return (int)(pitchY-(oppY/2));
        }

    }

    //Orth set/get
    public void setOrthogonal(boolean o){
      this.orth = o;  
    }
    public boolean getOrthogonal(){
      return orth;  
    }
    //Obstacle set/get
    public void avoidObstacle(boolean shouldAvoidObstacle) {
      avoid = shouldAvoidObstacle;
    };
    public boolean getAvoidObstacle(){
      return avoid;  
    }

    public void computePositions(WorldState ws) {
      if(!avoid){
        float pitchY = ws.getPitch().getPitchHeight();
        defPos = new Point2((int)(ws.getDefenderRobot().x), pitchY/2);
        attPos = new Point2((int)(ws.getAttackerRobot().x), pitchY/2);
        return;
      }
      if(orth){
          int ypos = getOrthogonal(ws); 
          defPos = new Point2((int)(ws.getDefenderRobot().x), ypos);
          attPos = new Point2((int)(ws.getAttackerRobot().x), ypos);
      } else {
          /*Passing at non orhtogonal angles would be done here for proper play, however the intereface and the interraction should be updated for this to
           * work.*/  
      }
      comp = true;
    };

    //This may need to add a delta range depending if it is possible to move with 1 unit precision.
    public boolean shouldAttackerMove(WorldState ws) {
        return !(ws.getAttackerRobot().y == attPos.getY() && ws.getAttackerRobot().x == attPos.getX());
    }
    public Point2 getAttackerPosition() {
        return attPos;
    }


    public Point2 getDefenderPosition() {
        return defPos;
    }

    /*
       Once the defender is in the X position and ready to catch, it should tell the attacker to kick.
     */
    public void setDefenderReady() {
        isDefenderReady = true;
    }
    public boolean isDefenderReady() {
        return isDefenderReady;
    }


    //Returns true if robots can pass in current situation, currently returns true if diff between Y values is less than 100 and obstacle is more than 100 away.
    public boolean canPass(WorldState ws){
        if(orth){
          return canPassOrth(ws);
        } else {
        	return false;
          //Check if the direct line between Attacker and Defender is blocked by an object.
        } 
    }
    public boolean canPassOrth(WorldState ws){
        float defY = ws.getDefenderRobot().y;
        float oppY = ws.getEnemyAttackerRobot().y;
        float attY = ws.getAttackerRobot().y;
        return Math.abs(defY-attY) < 100 && Math.abs(defY - oppY) > 100 && Math.abs(attY - oppY) > 100;
    }

    public void reset(){
      comp = false;
      isDefenderReady = false;
      attPos = new Point2(0, 0);
      defPos = new Point2(0, 0);
    }
    
    public boolean computed(){
        return comp;
    }
}
