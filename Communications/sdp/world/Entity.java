package sdp.world;

/**
 * Created by conrad on 03/02/15.
 */
public class Entity {
    private int width;
    private int length;
    private String name;
    private Vector velocity;
    private Vector position;

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public String getName() {
        return name;
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector newPosition) {
        position = newPosition;
    }

    public Entity(int widthN, int lengthN, String nameN) {
        width = widthN;
        length = lengthN;
        name = nameN;
    }
}
