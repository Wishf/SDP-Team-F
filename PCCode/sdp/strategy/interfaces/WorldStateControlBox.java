package sdp.strategy.interfaces;

import sdp.world.oldmodel.Point2;

/**
 * Created by conrad on 24/02/15.
 */
public interface WorldStateControlBox {

    /*
        Telling the WSCB if it should look for the obstacle when calculating the X position.
     */
    public void avoidObstacle(boolean shouldAvoidObstacle);
    public boolean getAvoidObstacle();
     
    //Setter and getter for the orthogonal flag, this makes the robots pass in a line paralles to the sides of the pitch. Mainly useful for milestone
    //and when no one is trying to block the pass.
    public void setOrthogonal(boolean orth);
    public boolean getOrthogonal();

    public void computePositions(WorldState ws);
    /*
        If we take the obstacle into consideration, the attacker will have to:
        1. grab the ball
        2. move to some position (to avoid the obstacle)
        3. Wait for the defender to be ready.

        shouldAttackerMove() returns true if the attacker's position needs to be adjusted.
        The x position is returned by 
     */

    public boolean shouldAttackerMove();
    public Point2 getAttackerPosition();

    /*
        DefenderXPosition returns:
         - the ball position if there's no obstacle
         - the computed optimal position if there obstacle is present
     */
    public Point2 getDefenderPosition();

    /*
       Once the defender is in the X position and ready to catch, it should tell the attacker to kick.
     */
    public void setDefenderReady();
    public boolean isDefenderReady();

    //Returns true if robots can pass in the current setup.
    public boolean canPass(WorldState ws){}
  
    //Resets the the positions and ready flags, also the internal state.
    public void reset();
}
