package world;

import java.awt.*;
import java.util.ArrayList;

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
                grid[i][j] = new GridPiece(3,0,new Point(i,j));
            }
        }
        grid[10][12] = new GridPiece(4,0,new Point(10,12));
        grid[10][11] = new GridPiece(0,0,new Point(10,11));
        grid[10][10] = new GridPiece(0,0,new Point(10,10));
        grid[10][9] = new GridPiece(2,0,new Point(10,9));
    }

    // Every time method updateWorld in class World executes, it will give the latest location
    public GridPiece[][] checkWorld(Point location)
    {
        this.location = location;
        updateCurrentGridLocation();
        updateGrid();

        return grid;
    }

    // updates the random grid
    public void updateGrid()
    {
        if(prevGridLocation.x != gridLocation.x || prevGridLocation.y != gridLocation.y)
        {








            prevGridLocation.x = gridLocation.x;
            prevGridLocation.y = gridLocation.y;
            System.out.println("PrevGridLocation x: " + prevGridLocation.x + " y: " + prevGridLocation.y);
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

    }

}
