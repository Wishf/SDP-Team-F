package sdp.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.strategy.ControlBox;

public class Milestone3AttackingStrategy extends GeneralStrategy {


    private BrickCommServer brick;
    private ControlThread controlThread;
    private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
    private boolean kicked;

    public Milestone3AttackingStrategy(BrickCommServer brick) {
        this.brick = brick;
        this.controlThread = new ControlThread();
        System.out.println("Starting.");
    }

    @Override
    public void stopControlThread() {
        this.controlThread.stop();
    }

    @Override
    public void startControlThread() {
        this.controlThread.start();
    }

    @Override
    public void sendWorldState(WorldState worldState) {
        super.sendWorldState(worldState);

        Point2 our_goal = ControlBox.controlBox.getAttackerPosition();
        MovingObject ball = worldState.getBall();

        boolean ballInAttackerArea = false;

        ballPositions.addLast(new Vector2f(ball.x, ball.y));
        if (ballPositions.size() > 3)
            ballPositions.removeFirst();

        if (ballX > leftCheck && ballX < rightCheck) {
            ballInAttackerArea = true;
        }

        boolean rotate = false;
        double targetAngle;
        double dx = 0;
        double dy = 0;

        Vector2f ball3FramesAgo = ballPositions.getFirst();
        float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
        float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

        if(!ballCaughtAttacker){
        	System.out.println("Getting the ball");
            dx = ballX - attackerRobotX;
            dy = ballY - attackerRobotY;
        }
        else {
        	System.out.println("Getting to the goal");
            dx = our_goal.getX() - attackerRobotX;
            dy = our_goal.getY() - attackerRobotY;

        }

        targetAngle = Math.toDegrees(Math.atan2(dy, dx)) % 360;

        if(targetAngle < 0){
            targetAngle += 360;
        }

        double angleDifference = (targetAngle - attackerOrientation) % 360;

        if(angleDifference < 0) {
            angleDifference += 360;
        }

        if(angleDifference > 180) {
            angleDifference -= 360;
        }

        if(Math.abs(angleDifference) > 25.0 ) {
            rotate = true;
        }

        double ballDistance = Math.sqrt(dx*dx+dy*dy);
        double catchThreshold = 35;
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;

        //System.out.println("bds "+ballDistanceSq);

        if(ballDistance < catchThreshold && !ballCaughtAttacker) {
            catch_ball = true;
            System.out.println("LALALA");
        }
        
        // If the ball slips from the catching area we can guess we did not catch it.
        if(ballCaughtAttacker && ballDistance > 10*catchThreshold) {
        	System.out.println("I've lost the ball!");
        	ballCaughtAttacker = false;
        }
        
        else if(ballCaughtAttacker && !kicked && ControlBox.controlBox.isDefenderReady()){
            // Here: need to check if the defender is ready and we don't need to move any further
            kick_ball = true;
        }

        boolean move_robot = false;


        if(!ballCaughtAttacker && ballInAttackerArea && ballDistance > 25) {
            move_robot = true;
            System.out.println("Need to move the robot since dY=" + ballDistance);
        }


        /*
        double slope = (ballY2 - ballY1) / ((ballX2 - ballX1) + 0.0001);
        double c = ballY1 - slope * ballX1;
        boolean ballMovement = Math.abs(ballX2 - ballX1) < 10;
        //int targetY = (int) (slope * defenderRobotX + c);
        //double ang1 = calculateAngle(defenderRobotX, defenderRobotY,
        //		defenderOrientation, defenderRobotX, defenderRobotY - 50);
        //ang1 = ang1 / 3;

*/

        synchronized (this.controlThread) {
            this.controlThread.operation.op = Operation.Type.DO_NOTHING;

            if (rotate) {
                this.controlThread.operation.op = Operation.Type.DEFROTATE;
                controlThread.operation.rotateBy = (int) (angleDifference);
            } else if (catch_ball) {
                System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (kick_ball) {
                System.out.println("Kick");
                this.controlThread.operation.op = Operation.Type.DEFKICK;
            } else if (uncatch) {
                System.out.println("Uncatch");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
            } else if (catch_ball) {
                System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = 13;
            }
        }

    }

    protected class ControlThread extends Thread {
        public Operation operation = new Operation();
        private ControlThread controlThread;
        private long kickTime;
        private long caughtTime;

        public ControlThread() {
            super("Robot control thread");
            setDaemon(true);
        }
        @Override


        public void run() {
        	
            try {
                while (true) {
                    Operation.Type op;
                    int rotateBy, travelDist;
                    System.out.println("???");
                    synchronized (this) {
                        op = this.operation.op;
                        rotateBy = this.operation.rotateBy;
                        travelDist = this.operation.travelDistance;
                    }
                    System.out.println("operation: " + op + " rotateBy: "
                            + rotateBy + " travelDist: " + travelDist);
                    switch (op) {
                        case DEFROTATE:
                            if (rotateBy != 0) {
                                brick.executeSync(new RobotCommand.Rotate(
                                        rotateBy, Math.abs(rotateBy)));
                            }
                            break;
                        case DEFTRAVEL:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Travel(
                                        travelDist / 3,
                                        Math.abs(travelDist) * 3 + 25));
                            }
                            break;
                        case DESIDEWAYS:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.TravelSideways(
                                        travelDist / 3,
                                        Math.abs(travelDist) * 3 + 25));
                            }
                            break;
                        case DEBACK:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Travel(
                                        travelDist,
                                        travelDist));
                            }
                        case DEFCATCH:
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                System.out.println("Catching");


                                brick.execute(new RobotCommand.Catch());
                                ballCaughtAttacker = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                            if((System.currentTimeMillis() - caughtTime > 1000)){
                                System.out.println("Kicking");

                                brick.execute(new RobotCommand.Kick(0));
                                Thread.sleep(500);
                                brick.execute(new RobotCommand.ResetCatcher());

                                kicked = true;
                                ballCaughtAttacker = false;
                                kickTime = System.currentTimeMillis();
                            }
                            break;
                        case DEFUNCATCH:
                            brick.execute(new RobotCommand.ResetCatcher());
                            break;
                        default:
                            break;
                    }
                    Thread.sleep(StrategyController.STRATEGY_TICK);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            finally {}

        }
    }

}
