package sdp.strategy;

import java.util.ArrayDeque;
import java.util.Deque;

import sdp.comms.BrickCommServer;
import sdp.comms.RobotCommand;
import sdp.comms.RobotCommand.TravelSideways;
import sdp.vision.Vector2f;
import sdp.vision.PitchConstants;
import sdp.vision.gui.tools.RobotDebugWindow;
import sdp.world.oldmodel.MovingObject;
import sdp.world.oldmodel.Point2;
import sdp.world.oldmodel.WorldState;
import sdp.strategy.interfaces.WorldStateControlBox;
import sdp.strategy.ControlBox;

public class FinalsAttackerStrategy extends GeneralStrategy {

    private BrickCommServer brick;
    private ControlThread controlThread;
    private Deque<Vector2f> ballPositions = new ArrayDeque<Vector2f>();
    private boolean kicked;
    private RotateDirection rotateDirection;
    private boolean hasBall;
    private boolean catcherReleased;
    private boolean initialized;
    private int control;
    private double previous_ball_distance = -1;

    public FinalsAttackerStrategy(BrickCommServer brick) {
        this.brick = brick;
        this.controlThread = new ControlThread();
        hasBall = false;
        control = 0;
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
    
    public float correctX() {
    	/*if(weAreShootingRight) {		
    		return attackerRobotX;
    	} else {
    		if(attackerRobotX - leftCheck < 10 || rightCheck - attackerRobotX < 10) {
    			return 250;
    		} else {
    			return attackerRobotX;
    		}
    	}*/
    	return attackerRobotX;
    }

    // Makes sure the robot does not bounce into the ball
    public Point2 targetFromBall(float ballX, float ballY) {
    	float side = ballY - 240;
    	if(side > 0) {
    		return new Point2(ballX, ballY-10);
    	} else {
    		return new Point2(ballX, ballY+10);
    	}
    }
    
    
    @Override
    public void sendWorldState(WorldState worldState) {
        super.sendWorldState(worldState);

        MovingObject ball = worldState.getBall();

        ballPositions.addLast(new Vector2f(ball.x, ball.y));
        if (ballPositions.size() > 3)
            ballPositions.removeFirst();
        
        boolean catch_ball = false;
        boolean kick_ball = false;
        boolean uncatch = false;
        boolean rotate = false;
        boolean travel_sideways = false;
        Point2 target = new Point2(attackerRobotX, attackerRobotY);
        double targetDistance = 0;
        float sidewaysThreshold = 20;
        double targetAngle = 0;
        boolean move_robot = false;
        double angleDifference = 0;
        
        float ball_dx = ballX - attackerRobotX;
        float ball_dy = ballY - attackerRobotY;
        double ballXYdistance = Math.sqrt(ball_dx*ball_dx+ball_dy*ball_dy);
        if(previous_ball_distance == -1) {
        	previous_ball_distance = ballXYdistance;
        }
        double catchThreshold = 35;
        
        //worldState.getEnemyDefenderRobot().orientation_angle
        
        if(isBallInAttackerArea(worldState)) {
        	
        	if(ballXYdistance > catchThreshold) {
        		hasBall = false;
        	}
        	
        	if(ballXYdistance < catchThreshold && previous_ball_distance > catchThreshold) {
        		uncatch = true;
        	}
        	
        	if(hasBall) {
        		//Rotate to the CB
        		ControlBox.controlBox.computeShot(worldState);
            	targetAngle = ControlBox.controlBox.getShootingAngle();
            	rotate = true;
            	kick_ball = true;
        	} else {
        		// Get to the target
        		target = targetFromBall(ballX, ballY);
        		RobotDebugWindow.messageAttacker.setMessage("Ball attacker b");
                if (catcherReleased != true) {
                	uncatch = true;
                }
                move_robot = true;
                
                if(ballXYdistance < catchThreshold) {
                	catch_ball = true;
                	move_robot = false;
                	rotate = false;
                }
        	}
        } else {
        	// Rotation at right angle
        	if(ballX > leftCheck) {
        		targetAngle = 0;
        	} else if(ballX < rightCheck) {
        		targetAngle = 180;
        	}
        	rotate = true;
        }
        
        if(isBallInDefenderArea(worldState)) {
        	ControlBox.controlBox.computePositions(worldState);
        	target = ControlBox.controlBox.getAttackerPosition();
        	travel_sideways = true;
            //Need to make sure we can catch the ball.
        	if (catcherReleased != true) {
        		uncatch = true;
        	}
        } else if(isBallInEnemyDefenderArea(worldState)) {
        	
        	target = new Point2(correctX(), ballY);
            if (catcherReleased != true) {
            	uncatch = true;
            }
            travel_sideways = true;
      	
        	
        } else if(isBallInEnemyAttackerArea(worldState)) {
        	// Follow the ball
        	target = new Point2(attackerRobotX, ballY);
    		travel_sideways = true;
    		
        }
        
/*        if (target.getY() >= bottomY) {
        	System.out.println("You've gone too far this time. The Y target was " + target.getY() + " and the actual bot is " + bottomY);
        	target.setY(bottomY - 40);
        	
        } else if (target.getY() < topY) {
        	System.out.println("You've gone too far this time. The Y target was " + target.getY() + " and the actual top is " + topY);        	
        	target.setY(topY + 40);
        }
        
        if (target.getX() <= leftCheck) {
        	System.out.println("You've gone too far this time. The X target was " + target.getX() + " and the actual left is " + leftCheck);        	
        	target.setX(leftCheck - 40);
        } else if (target.getX() >= rightCheck) {
        	System.out.println("You've gone too far this time. The X target was " + target.getX() + " and the actual right is " + rightCheck);        	
        	target.setX(rightCheck + 40);
        } NEW CODE IMPLEMENTED TO STOP IT GETTING STUCK BUT NOT TESTED YET SO COMMENTED */

        if(travel_sideways) {
        	
        	// If we're facing 0*
        	if(attackerOrientation < 90 || attackerOrientation > 270) {
        		targetDistance = target.getY() - attackerRobotY;
        	} else {
        		targetDistance = attackerRobotY - target.getY();
        	}
        	
    		if(Math.abs(targetDistance) < sidewaysThreshold)
    		{
    			travel_sideways = false;
    		}
        }
        
        if(move_robot) {
        	double dx = 0;
            double dy = 0;
           
            dx = target.getX() - attackerRobotX;
            dy = target.getY() - attackerRobotY;
            
            targetAngle = calcTargetAngle(-dx,-dy);
            
            targetDistance = Math.sqrt(dx*dx + dy*dy);
            
            rotate = true;
        }
 
        if(rotate) {
        	angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
        	
        	if(Math.abs(angleDifference) < 20) {
        		rotate = false;
        	}
        }
        
        if(kick_ball && rotate) {
        	kick_ball = false;
        }
        
        previous_ball_distance = ballXYdistance;
        
        
        /* 	        if(ballXYDistance < catchThreshold && !hasBall) {
            catch_ball = true;
            System.out.println("Catching");
        }
        
        // If the ball slips from the catching area we can guess we did not catch it.
        if(ballXYDistance > catchThreshold) {
        	hasBall = false;
        	uncatch = true;
        
        }
        if(!initialized) {
        	catch_balisBallInEnemyDefenderAreal = false;
        	rotate = false;
        	move_robot = false;
        	travel_sideways = false;
        	uncatch = true;
        	initialized = true;
        	catcherReleased = true;
        }*/

        synchronized (this.controlThread) {
            this.controlThread.operation.op = Operation.Type.DO_NOTHING;

            if (catch_ball) {
                RobotDebugWindow.messageAttacker.setMessage("Catch");
                this.controlThread.operation.op = Operation.Type.DEFCATCH;
            } else if (rotate) {
                this.controlThread.operation.op = Operation.Type.DEFROTATE;
                controlThread.operation.angleDifference = (int) angleDifference / 3;
            } else if (move_robot) {
                this.controlThread.operation.op = Operation.Type.DEFTRAVEL;
                controlThread.operation.travelDistance = (int) targetDistance;
            } else if(travel_sideways) {
            	this.controlThread.operation.op = Operation.Type.DESIDEWAYS;
            	controlThread.operation.travelDistance = (int) targetDistance;
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
                                brick.robotController.rotate(rotateBy);
                            }
                            break;
                        case DEFTRAVEL:
                            if (travelDist != 0) {
                                brick.robotController.travel(travelDist);
                                RobotDebugWindow.messageAttacker.setMessage("Travel by " + travelDist);
                            }
                            break;
                        case DESIDEWAYS:
                            if (travelDist != 0) {                            	
                                brick.robotController.travelSideways(travelDist);
                            }
                            break;
                        case DEBACK:
                            if (travelDist != 0) {
                                brick.robotController.travel(travelDist);
                            }
                        case DEFCATCH:
                        	System.out.println("Catch");
                            if((System.currentTimeMillis() - kickTime > 3000)){
                                //RobotDebugWindow.messageAttacker.setMessage("Catching");


                                brick.robotController.closeCatcher();
                                hasBall = true;
                                caughtTime = System.currentTimeMillis();
                                kicked = false;
                                catcherReleased = false;
                            }
                            
                            break;
                        case DEFKICK:
                        	System.out.println("Kick");
                            if((System.currentTimeMillis() - caughtTime > 1000)){
                                //RobotDebugWindow.messageAttacker.setMessage("Kicking");
                         
                                brick.robotController.kick();

                                kicked = true;
                                hasBall = false;
                                ballCaughtAttacker = false;
                                kickTime = System.currentTimeMillis();
                                ControlBox.controlBox.reset();
                                catcherReleased = true;
                            }
                            break;
                        case DEFUNCATCH:
                        	System.out.println("Uncatch");
                        	//if(!catcherReleased) {
                        		brick.robotController.openCatcher();
                                hasBall = false;
                                catcherReleased = true;
                             //   RobotDebugWindow.messageAttacker.setMessage("Uncatch");
                        	//}           
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
