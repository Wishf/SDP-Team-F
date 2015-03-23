package sdp.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.vision.Vector2f;
import sdp.vision.gui.tools.RobotDebugWindow;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.strategy.ControlBox;

enum Direction {
	CLOCKWISE, COUNTERCLOCKWISE, NONE
};

public class Milestone3AttackingStrategy extends GeneralStrategy {


    private BrickCommServer brick;
    private ControlThread controlThread;
    private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
    private boolean kicked;
    private RotateDirection rotateDirection;
    private boolean hasBall;

    public Milestone3AttackingStrategy(BrickCommServer brick) {
        this.brick = brick;
        this.controlThread = new ControlThread();
        hasBall = false;
        //RobotDebugWindow.messageAttacker.setMessage("Starting.");
    }
	
	public class RotateDirection {
		
		private Direction direction;
		public RotateDirection(Direction direction_) {
			direction = direction_;
		}
		
		public Direction getDirection() {
			return direction;
		}
		
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

        ballPositions.addLast(new Vector2f(ball.x, ball.y));
        if (ballPositions.size() > 3)
            ballPositions.removeFirst();

        boolean ballInAttackerArea = isBallInAttackerArea(worldState);
        boolean ballInDefenderArea = isBallInDefenderArea(worldState);
        boolean ballInEnemyDefenderArea = isBallInEnemyDefenderArea(worldState);
        boolean ballInEnemyAttackerArea = isBallInEnemyAttackerArea(worldState);
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
        boolean rotate = false;
        boolean travel_sideways = false;

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
            if(!hasBall) {
                target = new Point2(ballX, ballY);
                RobotDebugWindow.messageAttacker.setMessage("Ball attacker b");
            }
            uncatch = true;

        }
        
        if(ballInDefenderArea) {
            // Rotate to face the defender and ask the control box what to do.
        	ControlBox.controlBox.computePositions(worldState);
        	target = ControlBox.controlBox.getAttackerPosition();
            //RobotDebugWindow.messageAttacker.setMessage("Ball defender");
            //Need to make sure we can catch the ball.
            uncatch = true;
        }
        
        
        if(ballInEnemyDefenderArea) {
            // Follow the defender.
            target = new Point2(attackerRobotX, ballY);
            uncatch = true;
            //RobotDebugWindow.messageAttacker.setMessage("Ball enemy defender");
        }
        
        if(ballInEnemyAttackerArea) {
        	//Go to the middle
        	target = new Point2(attackerRobotX, 240);
        }

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
        
        double ballDistance = Math.sqrt(ball_dx*ball_dx+ball_dy*ball_dy);
        double targetDistance = Math.sqrt(dx*dx + dy*dy);
        double catchThreshold = 35;
        
        boolean move_robot = false;

        targetAngle = calcTargetAngle(dx,dy);
        
        if(ballInEnemyDefenderArea) {
        	if(worldState.weAreShootingRight) {
        		targetAngle = 0;
        	} else {
        		targetAngle = 180;
        	 }
        }
        
        if(ballInDefenderArea) {
        	if(worldState.weAreShootingRight) {
        		targetAngle = 180;
        	} else {
        		targetAngle = 0;
        	}
        }
        
        if(ballInAttackerArea && hasBall) {
        	targetAngle = ControlBox.controlBox.getShootingAngle();
        	System.out.println("Potato hasBall");
        }
        double angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
        
        if(ballInEnemyDefenderArea) {
        	if(Math.abs(targetDistance) > 25) {
        		travel_sideways = true;
        		targetDistance = attackerRobotY - ballY;
        	}
        } else if(ballInEnemyAttackerArea) {
        	if(Math.abs(targetDistance) > 25) {
        		//Going to the middle
        		travel_sideways = true;
        		targetDistance = 240 - attackerRobotY;
        	}
        } else {
	
	        if( (!hasBall && ballInAttackerArea && ballDistance > 25) 
	        		|| (targetDistance > 25)) {
	            move_robot = true;
	            uncatch = true;
	        } 
	        
	        if(ballInDefenderArea) {
	        	
	        	targetDistance = target.getY() - attackerRobotY;
	        	RobotDebugWindow.messageAttacker.setMessage("TargetY" + target.getY() + " My Y is " + attackerRobotY);
	        	if(targetDistance > 25) {
	        		travel_sideways = true;
	        		move_robot = false;
	        	}
	        }
        }
        
        double angleThreshold = 20d;
        
        if(Math.abs(angleDifference) > angleThreshold) {
        	rotate = true;
        }
        
        if(Math.abs(angleDifference) < angleThreshold) {
        	rotate = false;
        }
 
        if(!hasBall) {
        	System.out.println("Doesn't have the ball");
        }
        if(ballDistance < catchThreshold && !hasBall) {
            catch_ball = true;
            System.out.println("Catching");
        }
        
        // If the ball slips from the catching area we can guess we did not catch it.
        if(hasBall && ballDistance > 5*catchThreshold) {
        	hasBall = false;
        }
        else if(hasBall && !kicked && !move_robot && !rotate){
        	// We kick once we're ready. We don't need to wait for anyone.
            kick_ball = true;
        }

        if(rotate) {
        	 RobotDebugWindow.messageAttacker.setMessage("Rotating by " + angleDifference);
        } else if(catch_ball) {
       	     RobotDebugWindow.messageAttacker.setMessage("Catching FIRE");
        } else if(move_robot) {
        	 RobotDebugWindow.messageAttacker.setMessage("Going to " + target + " Current position " + new Point2(attackerRobotX, attackerRobotY));
        }
        
        
        if(kick_ball) {
        	RobotDebugWindow.messageAttacker.setMessage("Kicking the ball");        
        }
        
        if(travel_sideways) {
        	RobotDebugWindow.messageAttacker.setMessage("Travel sideways by " + targetDistance);
        }

        synchronized (this.controlThread) {
            this.controlThread.operation.op = Operation.Type.DO_NOTHING;

           if (rotate) {
                this.controlThread.operation.op = Operation.Type.DEFROTATE;
                controlThread.operation.angleDifference = (int) angleDifference / 3;
            } else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance;
            } else if(travel_sideways) {
            	this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
            	controlThread.operation.travelDistance = (int) targetDistance;
            } else if (catch_ball) {
                RobotDebugWindow.messageAttacker.setMessage("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (uncatch) {
                RobotDebugWindow.messageAttacker.setMessage("Uncatch");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
            } else if (kick_ball) {
                RobotDebugWindow.messageAttacker.setMessage("Kick");
                this.controlThread.operation.op = Operation.Type.DEFKICK;
            } 
        }

    }

    class ControlThread extends Thread {
        public Operation operation = new Operation();
        private ControlThread controlThread;
        private long kickTime;
        private long caughtTime;

        public ControlThread() {
            super("Robot control thread");
            setDaemon(true);
            System.out.println("Hello");
        }
        @Override


        public void run() {
        	
            try {
                while (true) {
                    Operation.Type op;
                    double rotateBy;
					double travelDist;
                    
                    synchronized (this) {
                        op = this.operation.op;
                        rotateBy = this.operation.angleDifference;
                        travelDist = this.operation.travelDistance;
                    }
                    RobotDebugWindow.messageAttacker.setMessage("operation: " + op);
                    switch (op) {
                        case DEFROTATE:
                            if (rotateBy != 0) {
                            	RobotDebugWindow.messageAttacker.setMessage("Rotate by "+ rotateBy);
                                brick.executeSync(new RobotCommand.Rotate(
                                        rotateBy));
                            }
                            break;
                        case DEFTRAVEL:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Travel(
                                        travelDist));
                                RobotDebugWindow.messageAttacker.setMessage("Travel by " + travelDist);
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
                                brick.execute(new RobotCommand.Travel(
                                        travelDist));
                            }
                        case DEFCATCH:
                        	System.out.println("Catch");
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //RobotDebugWindow.messageAttacker.setMessage("Catching");


                                brick.execute(new RobotCommand.ResetCatcher());
                                hasBall = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                            }
                            break;
                        case DEFKICK:
                        	System.out.println("Kick");
                            if((System.currentTimeMillis() - caughtTime > 1000)){
                                //RobotDebugWindow.messageAttacker.setMessage("Kicking");

                                brick.execute(new RobotCommand.Kick());
                                Thread.sleep(500);
                                brick.execute(new RobotCommand.Catch());

                                kicked = true;
                                hasBall = false;
                                ballCaughtAttacker = false;
                                kickTime = System.currentTimeMillis();
                                ControlBox.controlBox.reset();
                            }
                            break;
                        case DEFUNCATCH:
                        	System.out.println("Uncatch");
                            brick.execute(new RobotCommand.Catch());
                            hasBall = false;
                            RobotDebugWindow.messageAttacker.setMessage("Uncatch");
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
