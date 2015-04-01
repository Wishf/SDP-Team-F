/**
 * Name : MovingObject.java
 * Author : Dimitar Petrov
 * Description : Stores data relating to an object capable of movement:
 * Coordinates, velocity, angular orientation
 * */
package sdp.world.oldmodel;

import java.awt.geom.Point2D;

public class MovingObject {
	//x,y, representation on the grid
	//x,y are mm representations
	public float x;
	public float y;
	
	public double velocity;
	
	//Orientation coordinates
	public float orientation_angle;
	
	/**
	 * Initializes a moving object
	 * @param x represents the X coordinate
	 * @param y represents the Y coordinate
	 * @param angle represents the orientation angle of the object
	 * */
	public MovingObject(float x, float y, float angle){
		this.x = x;
		this.y = y;
		this.orientation_angle = angle;
	}
	
	/**
	 * Initializes a moving object
	 * @param x represents the X coordinate
	 * @param y represents the Y coordinate
	 * */
	public MovingObject(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public MovingObject() {
		x = 0;
		y = 0;
		orientation_angle = 0;
		velocity = 0;
	}

	public MovingObject copy(){
		return new MovingObject(x, y, this.orientation_angle);
	}
	
	public MovingObject copyTo(MovingObject m){
		m.orientation_angle = this.orientation_angle;
		m.x = this.x;
		m.y = this.y;	
		
		return m; 
	}
	
	public boolean equals(MovingObject m){
		return (m.x == this.x && m.y == this.y && m.orientation_angle == this.orientation_angle);
	}
	
	//rotating point of robots, need to represent
	//robot dimension extension from plates
	
	public Point2D asPoint() {
		return new Point2D.Double(x, y);
	}
}
