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
    private boolean lt_uncatch = false;
    private int previous_ball_counter = 0;
    private double previous_ball_distance = -1;
    private boolean readyToShoot = false;
    private Point2 shootingPosition;

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
    	if(attackerRobotX-leftCheck < 10) {
    		return attackerRobotX+10;
    	} else if(rightCheck-attackerRobotX < 10) {
    		return attackerRobotX-10;
    	} else {
    		return attackerRobotX;
    	}
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
    
    public Point2 shootAvoidObstacle() {
    	float side = enemyDefenderRobotY - 240;
    	
    	if(side > 0) {
    		return new Point2(attackerRobotX, 270);
    	} else {
    		return new Point2(attackerRobotX, 200);
    	}
    }
    
    public Point2 shootingTarget(float x, float y) {
    	if(y < 200) {
    		return new Point2(x, 200);
    	} else if(y > 250){
    		return new Point2(x, 250);
    	} else {
    		return new Point2(x, y);
    	}
    }
    
    public double getPiAngle(double ballX) {
    	if(ballX > leftCheck) {
    		return 0;
    	} else if(ballX < rightCheck) {
    		return 180;
    	} 
    	
    	return 0;
    	
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
        boolean rotate_to_defender = false;
        Point2 target = new Point2(attackerRobotX, attackerRobotY);
        double targetDistance = 0;
        float sidewaysThreshold = 20;
        double targetAngle = 0;
        boolean move_robot = false;
        double angleDifference = 0;
        boolean waitForShoot = false;
       
        
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
        		readyToShoot = false;
        	}  	
        	
        	if(hasBall) {
        		//Rotate to the CB
        		//if(readyToShoot) {
        		//	ControlBox.controlBox.computeShot(worldState);
                //	targetAngle = ControlBox.controlBox.getShootingAngle();
                //	RobotDebugWindow.messageAttacker.setMessage("RTS");
                //	rotate = true;
                //	
                //	waitForShoot = false;
        		//} else {
        			travel_sideways = true;
                	target = shootAvoidObstacle();//shootingTarget(attackerRobotX, attackerRobotY);
                	rotate = true;
                	targetAngle = getPiAngle(enemyDefenderRobotX);
                	//waitForShoot = true;
                	kick_ball = true;
                	System.out.println("Point position " + target + " defY " + enemyDefenderRobotY);
                	//RobotDebugWindow.messageAttacker.setMessage();
                	//RobotDebugWindow.messageAttacker.setMessage("Waiting for rts");
        		//}	           	
        	} else {
        		// Get to the target
        		target = targetFromBall(ballX, ballY);
                uncatch = true;
                move_robot = true;
                
                if(ballXYdistance < catchThreshold) {
                	catch_ball = true;
                	move_robot = false;
                	rotate = false;
                }
        	}
        } else {
        	// Rotation at right angle
        	targetAngle = getPiAngle(ballX);
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
        	
        	rotate_to_defender = true;
        	
        } else if(isBallInEnemyDefenderArea(worldState)) {
        	if(worldState.ballNotOnPitch) {
        		target = new Point2(correctX(), enemyDefenderRobotY);
        	} else {
        		target = new Point2(correctX(), ballY);
        	}
        	
            if (catcherReleased != true) {
            	uncatch = true;
            }
            travel_sideways = true;
      	
        	
        } else if(isBallInEnemyAttackerArea(worldState)) {
        	if(worldState.ballNotOnPitch) {
        		target = new Point2(attackerRobotX, enemyAttackerRobotY);
        	} else {
        		target = new Point2(attackerRobotX, ballY);
        	}
        	
    		travel_sideways = true;
    		
        }
        
        int border_threshold = 40;
        int border_control_agency = 50;
        
       if (target.getY() >= topY-border_threshold) {
        	System.out.println("A. The Y target was " + target.getY() + " and the actual bot is " + topY);
        	target.setY(topY - border_control_agency);
        	
        } else if (target.getY() < bottomY+border_threshold) {
        	System.out.println("B. The Y target was " + target.getY() + " and the actual top is " + bottomY);        	
        	target.setY(bottomY + border_control_agency);
        }
        
        if (target.getX() <= leftCheck+border_threshold) {
        	System.out.println("C. The X target was " + target.getX() + " and the actual left is " + leftCheck);        	
        	target.setX(leftCheck + border_control_agency);
        	
        } else if (target.getX() >= rightCheck-border_threshold) {
        	System.out.println("D. The X target was " + target.getX() + " and the actual right is " + rightCheck);        	
        	target.setX(rightCheck - border_control_agency);
        	
        } //NEW CODE IMPLEMENTED TO STOP IT GETTING STUCK BUT NOT TESTED YET SO COMMENTED */

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
        
        if(!travel_sideways && rotate_to_defender) {
        	targetAngle = calculateAngle(attackerRobotX, attackerRobotY, attackerOrientation, defenderRobotX, defenderRobotY);
        	rotate = true;
        }
        
        int margin = 20;
        /*if(attackerRobotX > leftCheck - margin) {
        	target = new Point2(leftCheck - margin, attackerRobotY);
        	move_robot = true;
        	travel_sideways = false;
        	
        } else if(attackerRobotX < rightCheck + margin) {
        	target = new Point2(rightCheck + margin, attackerRobotY);
        	move_robot = true;
        	travel_sideways = false;
        	
        }*/
        
        if(move_robot) {
        	double dx = 0;
            double dy = 0;
           
            dx = target.getX() - attackerRobotX;
            dy = target.getY() - attackerRobotY;
            
            targetAngle = calcTargetAngle(dx,dy);
            
            targetDistance = Math.sqrt(dx*dx + dy*dy);
            
            rotate = true;
        }
 
        if(rotate) {
        	angleDifference = calcAngleDiff(attackerOrientation, targetAngle);
        	
        	if(Math.abs(angleDifference) < 20) {
        		rotate = false;
        	}
        }
        
        if(kick_ball && (rotate || travel_sideways) ) {
        	kick_ball = false;
        }
        
        if(!travel_sideways && waitForShoot) {
        	readyToShoot = true;
        }
        
        if(ballXYdistance < catchThreshold && previous_ball_distance > catchThreshold) {
    		uncatch = true;
    		lt_uncatch = true;
    	}
        
        if(previous_ball_counter == 5) {
        	previous_ball_distance = ballXYdistance;
        	previous_ball_counter = 0;
        } else {
        	previous_ball_counter++;
        }
        
        
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
        	catch_ball = false;
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
            } else if (uncatch || lt_uncatch) {
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
                                brick.robotController.rotate(-rotateBy);
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
                                readyToShoot = false;
                            }
                            break;
                        case DEFUNCATCH:
                        	System.out.println("Uncatch");
                        	//if(!catcherReleased) {
                        		brick.robotController.openCatcher();
                                hasBall = false;
                                catcherReleased = true;
                                lt_uncatch = false;
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
