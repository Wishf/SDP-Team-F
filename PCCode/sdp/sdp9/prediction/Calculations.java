package sdp.sdp9.prediction;
import sdp.sdp9.strategy.GeneralStrategy;
import sdp.sdp9.vision.PitchConstants;
import sdp.sdp9.world.oldmodel.Point2;
import java.math.*;
import java.util.ArrayList;

public final class Calculations {
	
	/**
	 * 
	 * @return returns the distance between 2 points in a 2D plane
	 * */
	public static float GetDistance(Point2 a, Point2 b){
		double x1,x2,y1,y2;
		x1 = (double) a.getX();
		x2 = (double) b.getX();
		y1 = (double) a.getY();
		y2 = (double) b.getY();
		
		double distance = Math.sqrt(Math.pow((y1-y2),2)+Math.pow((x1-x2),2));
		return (float) distance;
	}
	
	/**
	 * returns the slope of a line determined by two points
	 * */
	public static float GetSlopeOfLine(Point2 a, Point2 b){
		return (a.getX()-b.getY())/(a.getY()-b.getY());		
	}
	/**
	 * [0] is the distance in moment t (current time - 1)
	 * [1] is the distance in moment t-1 (current time - 2)
	 * [2] is the distance in moment t-2 (current time - 3)
	 * */
	public static float LinearPrediction(float[] data){
		float v1,v2,v3,a1_2,a2_3, acc_decay;
		v1 = data[data.length-1];
		v2 = data[data.length-2];
		v3 = data[data.length-3];
	    a1_2 = Math.abs(v2-v1);
	    a2_3 = Math.abs(v3-v2);
	    acc_decay = a2_3 - a1_2;
	   // System.out.println(a1_2);
	    //System.out.println(a2_3);
	    //System.out.println(acc_decay);
		if(v1 - (a1_2 - acc_decay) > 0)
			return v1 - (a1_2 - acc_decay);
		else
			return 0;
	}
	
	public enum CorrectionType{
		TOP_OR_BOTTOM, LEFT_OR_RIGHT, 		
	}
	
	/**
	 * This method will calculate the coordinates of the end point, if its trajectory indicates it will
	 * bounce off a wall
	 * TODO: implement support for the corners
	 * */
	public static Point2 CalculateBounceCoordinate(Point2 prediction, CorrectionType type, float boundary){
		float c = prediction.getX(), d = prediction.getY();
		if(type == CorrectionType.TOP_OR_BOTTOM){
			float t = Math.abs(d - boundary);
			float y = Math.abs(d - 2*t);
			Point2 correction = new Point2(c,y);
			return correction;
		}
		else if(type == CorrectionType.LEFT_OR_RIGHT){
			float t = Math.abs(boundary - c);
			float x = Math.abs(c - 2*t);
			Point2 correction = new Point2(x,d);
			return correction;			
		}
		return null;
	}
	
	public static Point2 GetPointViaDistance(float distance, Point2 a, Point2 b){
		//odd case where a == b
		if(a.getX() == b.getX() && a.getY() == b.getY())
			return a;
		
		float dist = GetDistance(a,b);
		float sinA = Math.abs(a.getY()-b.getY())/dist;
		float cosA = Math.abs(a.getX()-b.getX())/dist;
		
		float x = b.getX()+cosA*distance;
		float y = b.getY()+sinA*distance;
		
		Point2 pred = new Point2(x,y);		
		return pred;		
	}
	
	
	public static Point2 PredictNextPoint(ArrayList<Point2> history){
		if(history.size() == 0)
			return new Point2(0,0);
		if(history.size() < 4){
			//if we have 1 point, need to have object orientation and apply a constant
			//if(history.size() > 0){
				//mock
				double orientation = 0.785;
				//initial kick multiplier. Needs to be adjusted
				double multiplier = 3;
				double tg = Math.tan(orientation);
				float X0 = history.get(0).getX();
				float Y0 = history.get(0).getY();
				double b = X0 -  Y0 / tg;
				//new point
				double X1,Y1;
				Y1 = Y0*multiplier;
				X1 = b + Y1/tg;
				
				Point2 prediction = new Point2((float)X1,(float)Y1);
				return prediction;
			//}
			//if we have 2 or 3 points, use a constant for prediction
			
			//throw new IllegalArgumentException("Cannot make a prediction based on less than 4 points");
		}
		else{
			int size = history.size();
			//compute distance travelled for the last 4 points
			float[] distances = new float[4];
			distances[0] = GetDistance(history.get(size - 4), history.get(size-3));
			distances[1] = GetDistance(history.get(size - 3), history.get(size-2));
			distances[2] = GetDistance(history.get(size - 2), history.get(size-1));
			//System.out.println(String.format("%f %f %f",distances[0], distances[1], distances[2]));
			//Get predicted distance
			float prediction = LinearPrediction(distances);
			//System.out.println(prediction);
			Point2 pred = GetPointViaDistance(prediction, history.get(size-2), history.get(size-1));
			
			
			return pred;
		}
	}

	/**
	 * returns the angle from which a bounce shot can be scored, from the position
	 * supplied
	 * @param bounce_direction 0 for automatic, 1 for top, -1 for bottom bounce
	 * */
	public static float GetBounceAngle(float robotX, float robotY,
			float robotOrientation, float targetX, float targetY, int bounce_direction, float pitchMid){
		
		int bottom_boundary = PitchConstants.getPitchOutlineBottom();
		int top_boundary = PitchConstants.getPitchOutlineTop();
		
		//int bottom_boundary = 20;
		//int top_boundary = 600;
		
		double robotRad = Math.toRadians(robotOrientation);
		if (robotRad > Math.PI)
			robotRad -= 2 * Math.PI;
		
		//Get X and Y velocities
		double x1 = (double) robotX;
		double y1 = (double) robotY;
		double x2 = (double) targetX;
		double y2 = (double) targetY;
		double y3;
		//check which wall we are bouncing off
		if(bounce_direction == 0)
		{
			if(Math.abs(y1) > pitchMid)
				y3 = bottom_boundary;
			else
				y3 = top_boundary;		
		}
		else if(bounce_direction == -1){
			y3 = bottom_boundary + 20;
		}
		else{
			y3 = top_boundary- 50;
		}
		
		double z = Math.abs(x1-x2);
		
		double a = Math.abs(y3-y2);
		double d = Math.abs(y3-y1);
		//calculate the coordinates of the middle point
		double c = (z*d)/(a+d);
		double b = z - c;
		
		double x3;
		if(x2 > x1)
			x3 = x1 + c;
		else
			x3 = x2 + b;
		
		//tangent values
		double tan_val_left = a/b;
		double tan_val_right = c/d;
		
		double left_angle = Math.atan(tan_val_left);
		double right_angle = Math.atan(tan_val_right);
		double choice_angle = right_angle;
		//REWORK IN PROGRESS
		choice_angle = GeneralStrategy.calculateAngle(robotX, robotY, robotOrientation, (float) x3, (float) y3);
		
		/*
		//if(left_angle > right_angle)
		//	choice_angle = right_angle + (left_angle - right_angle)*0.5;
		//else if(left_angle < right_angle)
		//	choice_angle = right_angle - (right_angle - left_angle)*0.5;
		
		//convert choice angle to the type the orientation is using
		double choice_angle_deg = Math.toDegrees(choice_angle);
		double adjusted_choice_angle = choice_angle_deg;
		if(choice_angle_deg < 90)
			adjusted_choice_angle = 
		
		
		float fl_choice_angle = (float) (Math.PI/2 - choice_angle);
		float angle_to_turn = (float) (fl_choice_angle - (robotRad));
		angle_to_turn = (float) Math.toDegrees(angle_to_turn);
		System.out.println("left angle: " + Math.toDegrees(left_angle) + " right angle: " + Math.toDegrees(right_angle) + " choice angle: " + Math.toDegrees(choice_angle) + " fl_choice_angle: " + Math.toDegrees(fl_choice_angle) + " angle to turn: " + angle_to_turn);
		//the angle the robot needs to turn
//		if(y3 == bottom_boundary)
//			//System.out.println("Bouncing off bottom boundary |Angle to turn = "+angle_to_turn);
//		if(y3 == top_boundary)
			//System.out.println("Bouncing off top boundary |Angle to turn = "+angle_to_turn);
		*/
		return (float)choice_angle;
	}
	
	private float CheckAngle(double x_position, double y_position, double x_velocity, double y_velocity, float[] goalCoordinates, float[] boundaries, int simulation_time){
		float top_boundary = boundaries[0];
		float bottom_boundary = boundaries[1];
		float left_boundary = boundaries[2];
		float right_boundary = boundaries[3];
		float goal_top_threshold = goalCoordinates[0];
		float goal_bottom_threshold = goalCoordinates[1];
		PitchConstants.getPitchOutlineBottom();
		for(int i=0; i< simulation_time; i++){
			//inc
		}
		
		return 0;
	}
}
