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
        //System.out.println("Starting.");
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

    public boolean isBallInDefenderArea(WorldState worldState) {
    	 return ballX < defenderCheck == worldState.weAreShootingRight;
    }
    
    public boolean isBallInEnemyDefenderArea(WorldState worldState) {
    	return (ballX > rightCheck && worldState.weAreShootingRight) || (ballX < leftCheck && !worldState.weAreShootingRight);
    }
    
    public boolean isBallInEnemyAttackerArea(WorldState worldState) {
    	return (ballX > defenderCheck && ballX < leftCheck && worldState.weAreShootingRight)
        		|| (ballX > rightCheck && ballX < defenderCheck && !worldState.weAreShootingRight);
    }
    
    public boolean isBallInAttackerArea(WorldState worldState) {
    	return (ballX > leftCheck && ballX < rightCheck);
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
            if(ballCaughtAttacker) {
            	ControlBox.controlBox.computePositions(worldState);
                //System.out.println("Ball attacker CB");
            } else {
                target = new Point2(ballX, ballY);
                //System.out.println("Ball attacker b");
            }
            //uncatch = true;

        }
        
        if(ballInDefenderArea) {
            // Rotate to face the defender and ask the control box what to do.
        	ControlBox.controlBox.computePositions(worldState);
        	target = ControlBox.controlBox.getAttackerPosition();
            //System.out.println("Ball defender");
            //Need to make sure we can catch the ball.
            uncatch = true;
        }
        
        if(ballInEnemyAttackerArea) {
            // Rotate to face the defender?
        	//System.out.println("Ball enemy attacker");
        	target = new Point2(attackerRobotX, ballY);
        }
        
        if(ballInEnemyDefenderArea) {
            // Follow the defender.
            target = new Point2(attackerRobotX, ballY);
            //System.out.println("Ball enemy defender");
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
        
        if(ballInAttackerArea) {
        	if(ballCaughtAttacker) {
        		targetAngle = ControlBox.controlBox.getShootingAngle();
        	}
        }
        	

        double angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
        
        if(ballInEnemyDefenderArea) {
        	if(Math.abs(targetDistance) > 25) {
        		travel_sideways = true;
        		targetDistance = attackerRobotY - ballY;
        	}
        } else if(ballInEnemyAttackerArea) {
        	if(Math.abs(targetDistance) > 25) {
        		travel_sideways = true;
        		targetDistance = ballY - attackerRobotY;
        	}
        } else {
	
	        if( (!hasBall && ballInAttackerArea && ballDistance > 25) 
	        		|| (targetDistance > 25)) {
	            move_robot = true;
	        } 
        
	        if(!move_robot && hasBall) {
	        	angleDifference = calcAngleDiff(attackerOrientation, ControlBox.controlBox.getShootingAngle());
	        	// If we dont have to move the robot anymore (it is in the shooting position)
	        	// we can try to rotate it to the shooting angle.
	        }
	        
	        if(ballInDefenderArea) {
	        	
	        	targetDistance = target.getY() - attackerRobotY;
	        	System.out.println("TargetY" + target.getY() + " My Y is " + attackerRobotY);
	        	if(targetDistance > 25) {
	        		travel_sideways = true;
	        		move_robot = false;
	        	}
	        }
        }
        
        double angleThreshold = 25d;
        
        if(Math.abs(angleDifference) > angleThreshold && rotate != true) {
        	rotate = true;
        }
        
        if(Math.abs(angleDifference) < angleThreshold) {
        	rotate = false;
        }
 
        if(ballDistance < catchThreshold && !hasBall) {
            catch_ball = true;
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
        	 System.out.println("Rotating by " + angleDifference);
        } else if(catch_ball) {
       	     System.out.println("Catching FIRE");
        } else if(move_robot) {
        	 System.out.println("Going to " + target + " Current position " + new Point2(attackerRobotX, attackerRobotY));
        }
        
        
        if(kick_ball) {
        	//System.out.println("Kicking the ball");        
        }
        
        if(travel_sideways) {
        	//System.out.println("Travel sideways by " + targetDistance);
        }

        synchronized (this.controlThread) {
            this.controlThread.operation.op = Operation.Type.DO_NOTHING;

           if (rotate) {
                this.controlThread.operation.op = Operation.Type.DEFROTATE;
                //controlThread.operation.rotateBy = (int) angleDifference //original code
                //if ((int)angleDifference > 30) { //new code requested by konrad, implemented by Patrick
                controlThread.operation.rotateBy = (int) angleDifference / 3;
                    //System.out.println("Rotating by 10 and angle difference is " + angleDifference);
                    
                //} else if ((int)angleDifference < -30) {
                //    controlThread.operation.rotateBy = -10;
               //     System.out.println("Rotating by -10 and angle difference is " + angleDifference);
               // }
            } else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance;
            } else if(travel_sideways) {
            	this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
            	controlThread.operation.travelDistance = (int) targetDistance;
            } else if (uncatch) {
                System.out.println("Uncatch");
                this.controlThread.operation.op = Operation.Type.DEFUNCATCH;
            } else if (catch_ball) {
                System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (kick_ball) {
                System.out.println("Kick");
                this.controlThread.operation.op = Operation.Type.DEFKICK;
            } else if (catch_ball) {
                System.out.println("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
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
                            	System.out.println("Rotate by "+ rotateBy);
                                brick.executeSync(new RobotCommand.Rotate(
                                        rotateBy));
                            }
                            break;
                        case DEFTRAVEL:
                            if (travelDist != 0) {
                                brick.execute(new RobotCommand.Travel(
                                        travelDist));
                                System.out.println("Travel by " + travelDist);
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
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //System.out.println("Catching");


                                brick.execute(new RobotCommand.Catch());
                                hasBall = true;
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
                                ControlBox.controlBox.reset();
                            }
                            break;
                        case DEFUNCATCH:
                            brick.execute(new RobotCommand.ResetCatcher());
                            System.out.println("Uncatch");
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
