package sdp.physics;

import sdp.world.*;
/**
 * Created by conrad on 03/02/15.
 */
public class PositionPrediction {
    private Entity object;
    public static Entity Pitch;
    public static int goalLength;

    public PositionPrediction(Entity objectN) {
        object = objectN;
    }

    /*

        Todo:
         - Pool definition
         - Measure pool and the goal
     */

    public Vector getPositionAt(int time) {
        /*
        Pool:
         X axis is longer (length)
         Y axis is shorter and has goals (width)
         */

        Vector position = object.getPosition();
        Vector velocity = object.getVelocity();

        double newX, newY;
        newX = position.getX() + time * velocity.getX();
        newY = position.getY() + time * velocity.getY();

        Vector newPosition = new Vector(newX, newY);

        if (insidePitch(newPosition)) {
            return newPosition;
        } else {
            reflect(newPosition);
        }

        return newPosition;
    }

    public Vector reflect(Vector position) {
        if(position.getY() < 0) {
            position.setY(Math.abs(position.getY()));
        } else if(position.getY() > Pitch.getWidth()) {
            position.setY(position.getY() - Pitch.getWidth());
        }

        if(position.getX() < 0) {
            position.setX(Math.abs(position.getX()));
        } else if(position.getX() > Pitch.getWidth()) {
            position.setX(position.getX() - Pitch.getLength());
        }

        if(insidePitch(position)) {
            return position;
        } else {
            return reflect(position);
        }
    }

    public boolean willScore(Vector position) {
        return (!insidePitchX(position.getX()) && insideGoalY(position.getY()));
    }

    private boolean insideGoalY(double y) {
        double difference = (Pitch.getWidth() - goalLength)/2;
        double upperBound = difference;
        double lowerBound = goalLength + difference;

        return (y > lowerBound && y < upperBound);
    }

    public boolean insidePitchX(double x) {
        return (x >= 0 && x <= Pitch.getLength());
    }
    public boolean insidePitchY(double y) {
        return (y >= 0 && y <= Pitch.getWidth());
    }
    public boolean insidePitch(Vector position) {
        return  insidePitchX(position.getX()) && insidePitchY(position.getY());
    }
}
