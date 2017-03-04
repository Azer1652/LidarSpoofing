package world;

import java.awt.*;
import java.util.ArrayList;
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
    //private int range = 10; // Range of the grid, 10 = 10 segments each direction + middle segment -> 441 segments
    private GridPiece[][] grid; // exists of blocks of 1000x1000 mm (Columns in rows)

    //private Segment straight

    // Receives the initial segments and location after building the world
    public RandomGen(Point location)
    {
        gridLocation = new Point(0,0);
        prevGridLocation = new Point(-1,-1);
        this.location = location;
        grid = new GridPiece[21][21];

        initGrid(); // initialisation of the grid
    }

    public void initGrid()
    {
        for(int i=0;i<21;i++)
        {
            for(int j=0;j<21;j++)
            {
                grid[i][j] = new GridPiece(5,0,new Point(i,j));
            }
        }
        // [row = y][column = x]                (x,y)
        //grid[20-9][10] = new GridPiece(4,0,new Point(10,9)); // End
        (grid[20-10][10] = new GridPiece(0,0,new Point(10,10))).makeSegment(); // Straight
        //grid[20-11][10] = new GridPiece(0,0,new Point(10,11)); // Straight
        //grid[20-12][10] = new GridPiece(2,0,new Point(10,12)); // TCross

        int chanceStraight = 15, chanceCorner = 15, chanceTCross = 20, chanceCross = 50; // Chances in percent
        boolean[] fitParameters;
        boolean isFitting,isFittingTop,isFittingBottom,isFittingLeft,isFittingRight;
        int tries;
        Random rand = new Random();
        int rType,rRotation; //random generated
        int type, rotation;
        Set<Point> prevPoints = new HashSet<>(); // Using sets to make sure no neighbor duplicates are taken
        Set<Point> currentPoints = new HashSet<>();
        Set<Point> newPoints = new HashSet<>();
        currentPoints.add(new Point(10,10)); // initial point: [row,column] grid

        for(int i=0; i<20; i++)
        {
            for(Point cp : currentPoints) // Makes neighbors of previous points
            {
                if(cp.x-1 > -1 )
                    newPoints.add(new Point(cp.x-1,cp.y));
                if(cp.x+1 < 21 )
                    newPoints.add(new Point(cp.x+1,cp.y));
                if(cp.y-1 > -1 )
                    newPoints.add(new Point(cp.x,cp.y-1));
                if(cp.y+1 < 21 )
                    newPoints.add(new Point(cp.x,cp.y+1));
            }
            newPoints.removeAll(prevPoints);

            for(Point np : newPoints)
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
                     * Shape    Chance
                     * Straight 30%
                     * Corner   10%
                     * TCross   20%
                     * Cross    40%
                     * End      0%
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
                    else // End shape
                    {
                        type = 4;
                        rotation = 0;
                    }

                    grid[np.x][np.y] = new GridPiece(type,rotation,new Point(np.y,20-np.x));
                    fitParameters = grid[np.x][np.y].getGridPieceFitBool(type, rotation);

                    if(np.y+1 < 21)
                    {
                        if (fitParameters[0] == grid[np.x][np.y+1].isBottom)
                            isFittingTop = true;
                    }
                    else
                        isFittingTop = true;

                    if(np.y-1 > -1)
                    {
                        if (fitParameters[1] == grid[np.x][np.y-1].isTop)
                            isFittingBottom = true;
                    }
                    else
                        isFittingBottom = true;

                    if(np.x+1 < 21)
                    {
                        if (fitParameters[2] == grid[np.x+1][np.y].isRight)
                            isFittingLeft = true;
                    }
                    else
                        isFittingLeft = true;

                    if(np.x-1 > -1)
                    {
                        if (fitParameters[3] == grid[np.x-1][np.y].isLeft)
                            isFittingRight = true;
                    }
                    else
                        isFittingRight = true;

                    if(isFittingTop && isFittingBottom && isFittingLeft && isFittingRight)
                    {
                        isFitting = true;
                        grid[np.x][np.y].makeSegment();
                    }
                    else
                        tries++;

                } while(!isFitting);// && tries < 25);
/*
                if(tries >= 10) // To improve performance, End shapes are made -> todo: needs long testing if this is really needed
                    (grid[np.x][np.y] = new GridPiece(4,0,new Point(np.y,20-np.x))).makeSegment();*/
            }

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
        //updateGrid();

        return grid;
    }

    // updates the random grid
    public void updateGrid()
    {
        if(prevGridLocation.x != gridLocation.x || prevGridLocation.y != gridLocation.y)
        {








            prevGridLocation.x = gridLocation.x;
            prevGridLocation.y = gridLocation.y;
            //System.out.println("PrevGridLocation x: " + prevGridLocation.x + " y: " + prevGridLocation.y);
        }
    }

    // This helps us keeping a track on where the car physically is in the grid
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

}
