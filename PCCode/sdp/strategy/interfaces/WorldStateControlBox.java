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

    public void computePositions(
            /*
            Add whatever arguments are needed.
             */
    );
    /*
        If we take the obstacle into consideration, the attacker will have to:
        1. grab the ball
        2. move to some position (to avoid the obstacle)
        3. Wait for the defender to be ready.

        shouldAttackerMove() returns true if the attacker's position needs to be adjusted.
        The x position is returned by AttackerXPosition()
     */

    public boolean shouldAttackerMove();
    public Point2 AttackerPosition();

    /*
        DefenderXPosition returns:
         - the ball position if there's no obstacle
         - the computed optimal position if there obstacle is present
     */
    public Point2 DefenderPosition();

    /*
       Once the defender is in the X position and ready to catch, it should tell the attacker to kick.
     */
    public void DefenderIsReady();
    public boolean isDefenderReady();

}
