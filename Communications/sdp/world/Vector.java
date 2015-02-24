package sdp.world;

/**
 * Created by conrad on 03/02/15.
 */
public class Vector {

    private double x,y;

    public Vector(double ex, double ey) {
        x = ex;
        y = ey;
    }

    public void setX(double newX) {
        x = newX;
    }

    public double getX() {
        return x;
    }

    public void setY(double newY) {
        y = newY;
    }

    public double getY() {
        return y;
    }
}
