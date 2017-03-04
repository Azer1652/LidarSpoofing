package world;

import java.awt.*;
import java.util.ArrayList;

/**
 * RandomGen created by Jan De Laet on 03/03/2017.
 */
public class GridPiece
{
    /** Predefined segments (always 1000 x 1000 size)
     *  rotation = 0 = normal; +1 = 90 rotated clockwise each
     *  Name        Shape       type        rotation
     *  Straight    ||          0           0,1
     *  Corner      /-          1           0,1,2,3
     *  TCross      T           2           0,1,2,3
     *  Cross       +           3           0
     *  End         |=|         4           0
     *  Init        like cross  5           0
     *
     *  Open sides for the different shapes and each rotation
     *              isTop       isBottom    isLeft  isRight
     *  Straight
     *  0           x           x
     *  1                                   x       x
     *
     *  Corner
     *  0                       x                   x
     *  1                       x           x
     *  2           x                       x
     *  3           x                               x
     *
     *  TCross
     *  0                       x           x       x
     *  1           x           x           x
     *  2           x                       x       x
     *  3           x           x                   x
     *
     *  Cross
     *  0           x           x           x       x
     *
     *  End
     *  0
     *
     *  Init (used for initialisation of the grid, same as Cross, but grid needs to be initialised first)
     *  0           x           x           x       x
     *
     *  Left vertical:      segments.add(new Segment(new double[]{ -10500+1000*x        , -10500+1000*y         },new double[]{ -10500+1000*x        , -10500+1000*y + 1000 }));
     *  Right vertical:     segments.add(new Segment(new double[]{ -10500+1000*x + 1000 , -10500+1000*y         },new double[]{ -10500+1000*x + 1000 , -10500+1000*y + 1000 }));
     *
     *  Top horizontal:     segments.add(new Segment(new double[]{ -10500+1000*x        , -10500+1000*y + 1000  },new double[]{ -10500+1000*x + 1000 , -10500+1000*y + 1000 }));
     *  Bottom horizontal:  segments.add(new Segment(new double[]{ -10500+1000*x        , -10500+1000*y         },new double[]{ -10500+1000*x + 1000 , -10500+1000*y        }));
     *
     */
    private int type; // type of predefined segment, -1 = unpredefined
    private int rotation; // only used for predefined segments, -1 = not used
    public boolean isTop = false, isBottom = false, isLeft = false, isRight = false; // if true, corresponding side is open = can fit
    public ArrayList<Segment> segments;
    private Point point; // what gridplace will the gridPiece get


    // Gridpiece constructor for predefined segments
    public GridPiece(int type, int rotation, Point p)
    {
        segments = new ArrayList<>();
        point = new Point(p);
        this.type = type;
        this.rotation = rotation;
        boolean[] fitParameters = getGridPieceFitBool(type,rotation);
        this.isTop = fitParameters[0];
        this.isBottom = fitParameters[1];
        this.isLeft = fitParameters[2];
        this.isRight = fitParameters[3];
    }

    public boolean[] getGridPieceFitBool(int type, int rotation)
    {
        boolean isTop = false, isBottom = false, isLeft = false, isRight = false; // if true, corresponding side is open = can fit
        boolean[] fitParameters = new boolean[4];

        switch (type)
        {
            case 0: // Straight
                if(rotation == 0)
                    isTop = isBottom = true;
                else
                    isLeft = isRight = true;
                break;
            case 1: // Corner
                switch(rotation)
                {
                    case 0: isBottom = isRight = true;
                        break;
                    case 1: isBottom = isLeft = true;
                        break;
                    case 2: isTop = isLeft = true;
                        break;
                    case 3: isTop = isRight = true;
                        break;
                }
                break;
            case 2: // TCross
                switch(rotation)
                {
                    case 0: isBottom = isLeft = isRight = true;
                        break;
                    case 1: isTop = isBottom =  isLeft = true;
                        break;
                    case 2: isTop = isLeft = isRight = true;
                        break;
                    case 3: isTop = isBottom = isRight = true;
                        break;
                }
                break;
            case 3: isTop = isBottom = isLeft = isRight = true; // Cross
                break;
            case 4: break; // End: Everything closed
            case 5: isTop = isBottom = isLeft = isRight = true; // Init
                break;
        }

        fitParameters[0] = isTop;
        fitParameters[1] = isBottom;
        fitParameters[2] = isLeft;
        fitParameters[3] = isRight;

        return fitParameters;
    }

    public void makeSegment()
    {
        if(!isTop)
            segments.add(new Segment(new double[]{ -10500+1000* point.x       , -10500+1000*point.y + 1000  },new double[]{ -10500+1000*point.x + 1000 , -10500+1000*point.y + 1000 }));

        if(!isBottom)
            segments.add(new Segment(new double[]{ -10500+1000*point.x        , -10500+1000*point.y         },new double[]{ -10500+1000*point.x + 1000 , -10500+1000*point.y        }));

        if(!isLeft)
            segments.add(new Segment(new double[]{ -10500+1000*point.x        , -10500+1000*point.y         },new double[]{ -10500+1000*point.x        , -10500+1000*point.y + 1000 }));

        if(!isRight)
            segments.add(new Segment(new double[]{ -10500+1000*point.x + 1000 , -10500+1000*point.y         },new double[]{ -10500+1000*point.x + 1000 , -10500+1000*point.y + 1000 }));
    }
}
