package tracing;

/**
 * Created by arthu on 20/02/2017.
 */
public class Hit {

    private double[] position;

    public double[] getPosition() {
        return position;
    }

    public void setPosition(double[] position) {
        this.position = position;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    private double time;

    public Hit(double[] position, double time){
        this.position = position;
        this.time = time;
    }
}
