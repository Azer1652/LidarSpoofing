package tracing;

import world.Segment;

/**
 * Created by arthu on 21/02/2017.
 */
public class BestHit {

    public Hit hit = null;
    public Segment segment = null;

    /**
     *
     * @param hit
     * @param segment
     */
    public BestHit(Hit hit, Segment segment){
        this.hit = hit;
        this.segment = segment;
    }
}
