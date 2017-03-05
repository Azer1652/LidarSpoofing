package world;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by arthu on 28/02/2017.
 */
public class LineComp extends JPanel{
    private CopyOnWriteArrayList<Segment> segments1;
    private List<Segment> segments2;

    public LineComp(List<Segment> segments){
        segments1 = new CopyOnWriteArrayList<>();
        segments2 = segments;
        segments1.addAll(segments2);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.clearRect(0,0,1000,1000);
        for(Segment s:segments1) {
            g.drawLine((int)s.start[0]/100+400, (int)s.start[1]/100+400, (int)s.end[0]/100+400, (int)s.end[1]/100+400);
        }
    }

    public void update(List<Segment> segments)
    {
        segments1 = new CopyOnWriteArrayList<>();
        segments2 = segments;
        segments1.addAll(segments2);
        repaint();
    }
}