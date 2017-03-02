package world;

import java.util.ArrayList;
import java.util.List;

/**
 * RandomGen created by Jan De Laet on 28/02/2017.
 */
//
public class RandomGen
{
    private double[] carLocation = new double[]{0,0};
    private double range = 10000; // World will generate new or delete old segments depending on the value
    private List<Segment> segments = new ArrayList<>();

    // Receives the initial segments and carlocation after building the world
    public RandomGen(List<Segment> segments, double[] carLocation)
    {
        this.segments = segments;
        this.carLocation = carLocation;
        generateSegments();
    }

    // Every time method updateWorld in class World executes, it will give the latest carlocation and segmentslist
    public void checkWorld(List<Segment> segments, double[] carLocation)
    {
        this.segments = segments;
        this.carLocation = carLocation;
        generateSegments();
        removeSegments();
    }

    // Will generate new segments when needed
    public void generateSegments()
    {
        for(Segment segment : segments)
        {
            //if(segment.)
        }
    }

    // Will remove old segments when needed
    public void removeSegments()
    {

    }

}
