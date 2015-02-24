package sdp.comms;

import java.io.IOException;

import sdp.comms.packets.*;
import sdp.util.DriveDirection;
import sdp.control.HolonomicRobotController;

public class RobotCommand {
    private static byte FORWARD_SPEED = (byte) 200;
    private static byte BACKWARD_SPEED = (byte) 200;
    private static byte STOP_SPEED = (byte) 0;
    private static int TIME_TO_MOVE_90_DEGREES = 500;
    private static byte ROTATION_SPEED = (byte) 150;
    
    private static HolonomicRobotController robotController = new HolonomicRobotController(null, false, 40,40,40);
    
	private RobotCommand() {		
	}

	public interface Command {
		public void sendToBrick(Radio radio)
				throws IOException;
	}

	private static abstract class GenericCommand implements Command {
		protected abstract Packet getOpcode();

		@Override
		public void sendToBrick(Radio radio)
				throws IOException {
			radio.sendPacket(getOpcode());
		}
	}
	
	// Classes below represent every possible brick command

	public static class Stop extends GenericCommand {
		@Override
		protected Packet getOpcode() {
            DriveDirection dir = DriveDirection.FORWARD;
            byte zero = 0;
			return new DrivePacket(zero, dir, zero, dir, zero, dir);
		}
	}

	public static class Kick extends GenericCommand {
		private byte speed;

		public Kick(int speed) {
			// The speed parameter is in percent, so we can scale it by multipling against 255
			this.speed = (byte)Math.round((speed / 100f) * 255);
		}

		@Override
		protected Packet getOpcode() {
			return new KickPacket(speed);
		}
	}

	public static class Catch extends GenericCommand {
		@Override
		protected Packet getOpcode() {
			return new EngageCatcherPacket();
		}
	}

	public static class Rotate extends GenericCommand {
		private int angle;
        private DriveDirection direction;
		private byte speed;

		public Rotate(int angle, int speed, boolean immediateReturn){
            
			this.angle = angle;
			// TODO: The speeds passed as parameters are tuned for group 9's robot; we'll need to scale them
			this.speed = (byte)speed;
		}
		
		public Rotate(int angle, int speed) {
			this(angle, speed, true);
		}

		@Override
		protected Packet getOpcode() {

			return robotController.rotate(angle, speed);
		}
	}

	public static class TravelArc extends GenericCommand {
		private double arcRadius;
		private int distance;
		private int speed;

		public TravelArc(double arcRadius, int distance, int speed) {
			this.arcRadius = arcRadius;
			this.distance = distance;
			this.speed = speed;
		}

		@Override
		protected Packet getOpcode() {
			return robotController.travelArc(arcRadius, distance, speed);
		}

	}

	public static class Travel extends GenericCommand {
		private int distance;
		private int travelSpeed;
		
		public Travel(int distance, int travelSpeed) {
			this.distance = distance;
			this.travelSpeed = travelSpeed;
		}
		
		@Override
		protected Packet getOpcode() {
			return robotController.travel(distance, travelSpeed);
		}
	}
	
	public static class TravelSideways extends GenericCommand {
		private int distance;
		private int travelSpeed;
		
		public TravelSideways(int distance, int travelSpeed) {
			this.distance = distance;
			this.travelSpeed = travelSpeed;
		}
		
		@Override
		protected Packet getOpcode() {
			return robotController.travelSideways(distance, travelSpeed);
		}
	}
	
	public static class ResetCatcher extends GenericCommand {
		@Override
		protected Packet getOpcode() {
			return new DisengageCatcherPacket();
		}
	}
}
