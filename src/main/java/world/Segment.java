package world;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arthu on 20/02/2017.
 */
public class Segment {

    public double start[];
    public double end[];

    public double position[];
    public double direction[];

    public List<Double[]> vertex;

    public Segment(double[] start, double[] end){
        this.start = start;
        this.end = end;

        this.position = start;
        this.direction = new double[]{end[0]-start[0], end[1]-start[1]};

        vertex=new ArrayList<>();
        vertex.add(new Double[]{start[0], 0d, start[1]});
        vertex.add(new Double[]{start[0], 10d, start[1]});
        vertex.add(new Double[]{end[0], 10d, end[1]});
        vertex.add(new Double[]{end[0], 0d, end[1]});
        //normalize();
    }

    private void normalize(){
        double length = Math.sqrt(direction[0]*direction[0]+direction[1]*direction[1]);
        direction[0] /= length;
        direction[1] /= length;
    }

    public List<Double[]> getVertex(){
        return vertex;
    }

    /*
    @Override
    public boolean equals(Object o){
        if(o.getClass() == Segment.class){
            Segment s = (Segment) o;
            if(s.start == this.start && s.end == this.end){
                return true;
            }
        }
        return false;
    }
    */
}
