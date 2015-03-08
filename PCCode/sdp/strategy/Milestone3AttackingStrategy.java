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
        //System.out.println("Starting.");
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

        MovingObject ball = worldState.getBall();

        boolean ballInAttackerArea = false;

        ballPositions.addLast(new Vector2f(ball.x, ball.y));
        if (ballPositions.size() > 3)
            ballPositions.removeFirst();

        boolean ballInDefenderArea = false;
        boolean ballInEnemyDefenderArea = false;
        boolean ballInEnemyAttackerArea = false;

        if (ballX < defenderCheck == worldState.weAreShootingRight) {
            ballInDefenderArea = true;
        }

        if((ballX > rightCheck && worldState.weAreShootingRight) || (ballX > leftCheck && !worldState.weAreShootingRight)) {
            ballInEnemyDefenderArea = true;
        }

        if (ballX > leftCheck && ballX < rightCheck) {
            ballInAttackerArea = true;
        }

        if(!ballInAttackerArea && !ballInEnemyDefenderArea && !ballInDefenderArea) {
            ballInEnemyAttackerArea = true;
        }

        /*
        If ball in attacker area -> catch it and attack
        If ball in our defender area -> rotate and ask the CB where to go
        If ball in enemy defender area -> follow the enemy defender (block)
        If ball in enemy attacker area -> do nothing. They won't pass it back.
         */

        Point2 target = new Point2(attackerRobotX, attackerRobotY);

        if(ballInAttackerArea) {
            // 1. Catch the ball
            // 2. Ask the control box where to go.
            if(ballCaughtAttacker) {
                target = ControlBox.controlBox.getAttackerPosition();
            } else {
                target = new Point2(ballX, ballY);
            }

        }
        if(ballInDefenderArea) {
            // Rotate to face the defender and ask the control box what to do.
            target = ControlBox.controlBox.getAttackerPosition();
        }
        if(ballInEnemyAttackerArea) {
            // Rotate to face the defender?

        }
        if(ballInEnemyDefenderArea) {
            // Follow the defender.
            target = new Point2(attackerRobotX, enemyAttackerRobotY);
        }

        boolean rotate = false;
        double targetAngle;
        double dx = 0;
        double dy = 0;

        //Vector2f ball3FramesAgo = ballPositions.getFirst();
        //float ballX1 = ball3FramesAgo.x, ballY1 = ball3FramesAgo.y;
        //float ballX2 = worldState.getBall().x, ballY2 = worldState.getBall().y;

        float ball_dx = ballX - attackerRobotX;
        float ball_dy = ballY - attackerRobotY;
        
        dx = target.getX() - attackerRobotX;
        dy = target.getY() - attackerRobotY;

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

        ////System.out.println("Ball position " + ballX + " - " + ballY);
        if(Math.abs(angleDifference) > 10.0 ) {
        	//System.out.print("At" + targetAngle + " Ao" + attackerOrientation);
            rotate = true;
        }

        double ballDistance = Math.sqrt(ball_dx*ball_dx+ball_dy*ball_dy);
        double targetDistance = Math.sqrt(dx*dx + dy*dy);
        double catchThreshold = 35;
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
 
        if(ballDistance < catchThreshold && !ballCaughtAttacker) {
            catch_ball = true;
        }
        
        // If the ball slips from the catching area we can guess we did not catch it.
        if(ballCaughtAttacker && ballDistance > 5*catchThreshold) {
        	ballCaughtAttacker = false;
        }
        else if(ballCaughtAttacker && !kicked && ControlBox.controlBox.isDefenderReady()){
            kick_ball = true;
        }

        boolean move_robot = false;

        if( (!ballCaughtAttacker && ballInAttackerArea && ballDistance > 25) 
        		|| (targetDistance > 25)) {
            move_robot = true;
        }
        

        synchronized (this.controlThread) {
            this.controlThread.operation.op = Operation.Type.DO_NOTHING;

            if (rotate) {
                this.controlThread.operation.op = Operation.Type.DEFROTATE;
                controlThread.operation.rotateBy = (int) (angleDifference);
            } else if (catch_ball) {
                //System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (kick_ball) {
                //System.out.println("Kick");
                this.controlThread.operation.op = Operation.Type.DEFKICK;
            } else if (uncatch) {
                //System.out.println("Uncatch");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
            } else if (catch_ball) {
                //System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance;
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
                    
                    synchronized (this) {
                        op = this.operation.op;
                        rotateBy = this.operation.rotateBy;
                        travelDist = this.operation.travelDistance;
                    }
                    //System.out.println("operation: " + op);
                    switch (op) {
                        case DEFROTATE:
                            if (rotateBy != 0) {
                                brick.executeSync(new RobotCommand.Rotate(
                                        rotateBy));
                            }
                            break;
                        case DEFTRAVEL:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Trave(
                                        travelDist));
                            }
                            break;
                        case DESIDEWAYS:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.TravelSideways(
                                        travelDist));
                            }
                            break;
                        case DEBACK:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Trave(
                                        travelDist));
                            }
                        case DEFCATCH:
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //System.out.println("Catching");


                                brick.execute(new RobotCommand.Catch());
                                ballCaughtAttacker = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                            if((System.currentTimeMillis() - caughtTime > 1000)){
                                //System.out.println("Kicking");

                                brick.execute(new RobotCommand.Kick());
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
