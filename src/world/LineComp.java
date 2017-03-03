package world;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by arthu on 28/02/2017.
 */
public class LineComp extends Component{

    java.util.List<Segment> segments;

    public LineComp(List<Segment> segments){
        this.segments = segments;
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        for(Segment s:segments) {
            g.drawLine((int)s.start[0]/100+400, (int)s.start[1]/100+400, (int)s.end[0]/100+400, (int)s.end[1]/100+400);
        }
    }
}