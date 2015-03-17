/**	====================HOW-TO===============
 * 1) To not recompute everything endlessly, check if computed() is true, if it is, then there are already
 * positions available for use.
 * Note: compute should be called by defender, since it needs to be called when ball is obtained. Attacker
 * should wait until something has been computed and then react. 
 * 
 * 2) The return values of (0, something) may be be caused of the vision skips the robot position in a frame.
 * Adding logic like "if return_value == some_unacceptable_value ....." is not appropriate here for several
 * reasons (does not make sense to put it here and this cannot get a new frame by itself, strategy needs
 * to check this stuff anyways). The * strategy should check if value is ok and a frame was not skipped. If
 * it was, it should call reset() and try again. 
 * 
 * 3) Before shooting, canPass() should be called to check if the positions are ok, if not, then
 * call reset() and recompute it again or some other logic may be applied in strategy.
 * 
 * 4) After a pass has been made (or any kick, since there is no way we still have ball after kicking,
 * unless there was a hardware malfunction), we should call reset(). This should be put straight after
 * the kick packet has been sent.
 * 
 *  Note: reset is needed to keep all strategies suing it synchronized and also to reset flags like
 *  "defender ready". 
 * 
 * 5) For shooting. Angle 0.0 is pointing along in the X axis. So 0.0 is pointing orthogonally at the goal.
 * =============================================== 
 */
package sdp.strategy.interfaces;

import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;

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

    public boolean shouldDefenderMove(WorldState ws);
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
    public void setAttackerReady();
    public boolean isAttackerReady();

    //Returns true if robots can pass in the current setup.
    public boolean canPass(WorldState ws);
  
    //Resets the the positions and ready flags, also the internal state.
    public void reset();
    public boolean computed();
    
    // Returns the position from which to shoot (attacker).
    public Point2 getShootingPosition();
    
    // Returns the angle from which the attacking robot needs to shoot.
    public double getShootingAngle();
    
    public void computeShot(WorldState ws);
}
