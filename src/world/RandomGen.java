package world;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * RandomGen created by Jan De Laet on 28/02/2017.
 */
//
public class RandomGen
{
    private Point location;
    private Point gridLocation;
    private Point prevGridLocation;
    private int range = 10; // Range of the grid, 10 = 10 segments each direction + middle segment -> 441 segments
    //todo: Currently range is hardcoded, make it dynamic
    private GridPiece[][] grid; // exists of blocks of 1000x1000 mm (Columns in rows)

    // Receives the initial segments and location after building the world
    public RandomGen(Point location)
    {
        gridLocation = new Point(0,0);
        prevGridLocation = new Point(-1,-1);
        this.location = location;
        grid = new GridPiece[2*range+1][2*range+1];

        initGrid(); // initialisation of the grid
    }

    public void initGrid()
    {
        for(int i=0;i<2*range+1;i++)
        {
            for(int j=0;j<2*range+1;j++)
            {
                grid[i][j] = new GridPiece(5,0,new Point(i,j));
            }
        }
        // [row = y][column = x]                (x,y)
        //grid[20-9][10] = new GridPiece(4,0,new Point(10,9)); // End
        (grid[2*range-range][range] = new GridPiece(0,0,new Point(range,range))).makeSegment(); // Straight
        //grid[20-11][10] = new GridPiece(0,0,new Point(10,11)); // Straight
        //grid[20-12][10] = new GridPiece(2,0,new Point(10,12)); // TCross

        Set<Point> prevPoints = new HashSet<>(); // Using sets to make sure no neighbor duplicates are taken
        Set<Point> currentPoints = new HashSet<>();
        Set<Point> newPoints = new HashSet<>();
        currentPoints.add(new Point(range,range)); // initial point: [row,column] grid

        for(int i=0; i<2*range; i++)
        {
            for(Point cp : currentPoints) // Makes neighbors of previous points
            {
                if(cp.x-1 > -1 )
                    newPoints.add(new Point(cp.x-1,cp.y));
                if(cp.x+1 < 2*range+1 )
                    newPoints.add(new Point(cp.x+1,cp.y));
                if(cp.y-1 > -1 )
                    newPoints.add(new Point(cp.x,cp.y-1));
                if(cp.y+1 < 2*range+1 )
                    newPoints.add(new Point(cp.x,cp.y+1));
            }
            newPoints.removeAll(prevPoints);

            generateRandomGridPieces(newPoints); // Algorithm

            prevPoints.clear();
            prevPoints.addAll(currentPoints);
            currentPoints.clear();
            currentPoints.addAll(newPoints);
            newPoints.clear();
            //System.out.println("prevPoints: " + prevPoints.size());
            //System.out.println("currentPoints: " + currentPoints.size());
        }
    }

    // Every time method updateWorld in class World executes, it will give the latest location
    public GridPiece[][] checkWorld(Point location)
    {
        this.location = location;
        //System.out.println("Physical Location x: " + location.x + " y: " + location.y);
        updateCurrentGridLocation();
        updateGrid();

        return grid;
    }

    // updates the random grid
    public void updateGrid()
    {
        Set<Point> points = new HashSet<>();

        if(gridLocation.x > prevGridLocation.x) // Column needs to be added on the right
        {
            // Move every column to the left in the Grid
            for(int i=0; i<2*range; i++)
            {
                for(int j=0; j<2*range+1; j++)
                {
                    grid[j][i] = grid[j][i+1];
                }
            }

            // Let the algorithm random generate GridPieces in the most right column
            for(int z=0; z<2*range+1; z++)
            {
                points.add(new Point(z,2*range)); // [row,column] grid
            }
        }
        else if(gridLocation.x < prevGridLocation.x)  // Column needs to be added on the left
        {
            for(int i=2*range; i>0; i--)
            {
                for(int j=0; j<2*range+1; j++)
                {
                    grid[j][i] = grid[j][i-1];
                }
            }

            for(int z=0; z<2*range+1; z++)
            {
                points.add(new Point(z,0)); // [row,column] grid
            }
        }

        if(gridLocation.y > prevGridLocation.y)  // Row needs to be added on the top
        {
            for(int i=2*range; i>0; i--)
            {
                for(int j=0; j<2*range+1; j++)
                {
                    grid[i][j] = grid[i-1][j];
                }
            }

            for(int z=0; z<2*range+1; z++)
            {
                points.add(new Point(0,z)); // [row,column] grid
            }
        }
        else if(gridLocation.y < prevGridLocation.y) // Row needs to be added on the bottom
        {
            for(int i=0; i<2*range; i++)
            {
                for(int j=0; j<2*range+1; j++)
                {
                    grid[i][j] = grid[i+1][j];
                }
            }

            for(int z=0; z<2*range+1; z++)
            {
                points.add(new Point(2*range,z)); // [row,column] grid
            }
        }

        generateRandomGridPieces(points); // if car hasn't moved, nothing will be generated
        prevGridLocation.x = gridLocation.x;
        prevGridLocation.y = gridLocation.y;
        //System.out.println("PrevGridLocation x: " + prevGridLocation.x + " y: " + prevGridLocation.y);
    }

    // This helps us keeping a track on where the car physically is in the grid (Here: real x/y coordinates!)
    public void updateCurrentGridLocation()
    {
        if(location.x > 500+1000*gridLocation.x)
        {
            gridLocation.x++;
        }
        else if(location.x < -500+1000*gridLocation.x)
        {
            gridLocation.x--;
        }

        if(location.y > 500+1000*gridLocation.y)
        {
            gridLocation.y++;
        }
        else if(location.y < -500+1000*gridLocation.y)
        {
            gridLocation.y--;
        }

        //System.out.println("Gridlocation x: " + gridLocation.x + " y: " + gridLocation.y);
    }

    // Algorithm used in initialisation and update of the mapgrid
    public void generateRandomGridPieces(Set<Point> points)
    {
        int chanceStraight = 5, chanceCorner = 5, chanceTCross = 5, chanceCross = 50, chanceEnd = 5,
                chanceDiagonalCross = 5, chanceDiagonal = 5, chanceObsTriangle = 15; // Chances in percent
        boolean[] fitParameters;
        boolean isFitting,isFittingTop,isFittingBottom,isFittingLeft,isFittingRight;
        int tries;
        Random rand = new Random();
        int rType,rRotation; //random generated
        int type, rotation;

        for(Point p : points)
        {
            isFitting = false;
            isFittingTop = false;
            isFittingBottom = false;
            isFittingLeft = false;
            isFittingRight = false;
            tries = 0;
            do
            {
                // Possibility to favor one shape more than the other
                rType = rand.nextInt(100) + 1; // type
                rRotation = rand.nextInt(100) + 1; // rotation
                /**
                 * Shape            Chance
                 * Straight         10%
                 * Corner           10%
                 * TCross           15%
                 * Cross            20%
                 * End              5%
                 * DiagonalCross    10%
                 * Diagonal         10%
                 * ObsTriangle      20%
                 *
                 * Some confusion with the grid row/column vs the actual coordinates, here x/y means row/column!
                 */
                if(rType > 0 && rType <= chanceStraight) // Straight shape
                {
                    if(rRotation > 0 && rRotation <= 50) // Rotation ||
                    {
                        type = 0;
                        rotation = 0;
                    }
                    else // Rotation =
                    {
                        type = 0;
                        rotation = 1;
                    }
                }
                else if(rType > chanceStraight && rType <= chanceStraight+chanceCorner) // Corner shape
                {
                    if(rRotation > 0 && rRotation <= 25) // Rotation /-
                    {
                        type = 1;
                        rotation = 0;
                    }
                    else if(rRotation > 25 && rRotation <= 50) // 90° clockwise rotated
                    {
                        type = 1;
                        rotation = 1;
                    }
                    else if(rRotation > 50 && rRotation <= 75) // 180° clockwise rotated
                    {
                        type = 1;
                        rotation = 2;
                    }
                    else // 270° clockwise rotated
                    {
                        type = 1;
                        rotation = 3;
                    }
                }
                else if(rType > chanceStraight+chanceCorner && rType <= chanceStraight+chanceCorner+chanceTCross) // TCross shape
                {
                    if(rRotation > 0 && rRotation <= 25) // Rotation /-
                    {
                        type = 2;
                        rotation = 0;
                    }
                    else if(rRotation > 25 && rRotation <= 50) // 90° clockwise rotated
                    {
                        type = 2;
                        rotation = 1;
                    }
                    else if(rRotation > 50 && rRotation <= 75) // 180° clockwise rotated
                    {
                        type = 2;
                        rotation = 2;
                    }
                    else // 270° clockwise rotated
                    {
                        type = 2;
                        rotation = 3;
                    }
                }
                else if(rType > chanceStraight+chanceCorner+chanceTCross && rType <= chanceStraight+chanceCorner+chanceTCross+chanceCross) // Cross shape
                {
                    type = 3;
                    rotation = 0;
                }
                else if(rType > chanceStraight+chanceCorner+chanceTCross+chanceCross && rType <= chanceStraight+chanceCorner+chanceTCross+chanceCross+chanceEnd) // End shape
                {
                    type = 4;
                    rotation = 0;
                }
                else if(rType > chanceStraight+chanceCorner+chanceTCross+chanceCross+chanceEnd &&
                        rType <= chanceStraight+chanceCorner+chanceTCross+chanceCross+chanceEnd+chanceDiagonalCross) // DiagonalCross shape
                {
                    type = 6;
                    rotation = 0;
                }
                else if(rType > chanceStraight+chanceCorner+chanceTCross+chanceCross+chanceEnd+chanceDiagonalCross &&
                        rType <= chanceStraight+chanceCorner+chanceTCross+chanceCross+chanceEnd+chanceDiagonalCross+chanceDiagonal) // Diagonal shape
                {
                    if(rRotation > 0 && rRotation <= 50) // Rotation /
                    {
                        type = 7;
                        rotation = 0;
                    }
                    else // Rotation \
                    {
                        type = 7;
                        rotation = 1;
                    }
                }
                else // Obstacle Triangle shape
                {
                    type = 8;
                    rotation = 0;
                }

                //Makes sure the gridLocation of the car is looked at before physically making the gridPiece
                grid[p.x][p.y] = new GridPiece(type,rotation,new Point(p.y+gridLocation.x,2*range-p.x+gridLocation.y));

/**
 * Below code: Not really needed actually, because of the chances per shape, you still have a very good maze where you can find your way through

                fitParameters = grid[p.x][p.y].getGridPieceFitBool(type, rotation);

                if(p.y+1 < 21)
                {
                    if (fitParameters[0] == grid[p.x][p.y+1].isBottom)
                        isFittingTop = true;
                }
                else
                    isFittingTop = true;

                if(p.y-1 > -1)
                {
                    if (fitParameters[1] == grid[p.x][p.y-1].isTop)
                        isFittingBottom = true;
                }
                else
                    isFittingBottom = true;

                if(p.x+1 < 21)
                {
                    if (fitParameters[2] == grid[p.x+1][p.y].isRight)
                        isFittingLeft = true;
                }d
                else
                    isFittingLeft = true;

                if(p.x-1 > -1)
                {
                    if (fitParameters[3] == grid[p.x-1][p.y].isLeft)
                        isFittingRight = true;
                }
                else
                    isFittingRight = true;

                if(isFittingTop && isFittingBottom && isFittingLeft && isFittingRight)
                {
                    isFitting = true;
                    grid[p.x][p.y].makeSegment();
                }
                else
                    tries++;
*/
                isFitting = true;
                grid[p.x][p.y].makeSegment();
            } while(!isFitting);// && tries < 25);
/*
                if(tries >= 10) // To improve performance, End shapes are made -> todo: needs long testing if this is really needed
                    (grid[np.x][np.y] = new GridPiece(4,0,new Point(np.y,20-np.x))).makeSegment();*/
        }
    }

}
