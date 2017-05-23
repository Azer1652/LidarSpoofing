package world;

import tracing.Hit;
import tracing.Ray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class World {

    //This value is supposed to be divided by -pi/4, but it generates an offset if you don't substract 0.1
    private final static double angleStartRad = +Math.toRadians(135); //-0.28;
    //private final static double angleEndRad = Math.PI-angleStartRad;
    private final static double angleDiffRad = Math.toRadians(270)/(1080);
    private final String args;

    private int[][] pixelData;
    private Image image;

	public ArrayList<Segment> segments = new ArrayList<>();
	private double data[] = new double[1080];
    private StringBuilder dataString = new StringBuilder();

    private double[] carLocation;
    private double currentCarAngleRad = 0;

    public double speed;
    public double turnSpeed = 0.05;

    private boolean moveLikeCar = true;
    private boolean up, down, left, right;

    private RandomGen randomGen;
    private JFrame testFrame;
    private LineComp lineComp;
    private Mode mode;


	public World(String args){
	    up = false;
        down = false;
        left = false;
        right = false;
        this.args = args;
        checkMode();
        if(mode == Mode.RANDOM)
            new SDF(segments,randomGen.range); // Makes sdformat file
        else
            new SDF(segments,-51.224998); // Makes sdformat file <-- ENTER ORIGIN FROM YAML FILE HERE
		updateWorld();
		encodeData();
	}
	
	public String getDataString(){
	    updateWorld();
	    encodeData();
	    return dataString.toString();
    }

    private Hit tracePixel(double angle){
        double traceAngle =angle + currentCarAngleRad;

        traceAngle = traceAngle % (2*Math.PI);
        if(traceAngle < -Math.PI)
        {
            traceAngle = traceAngle + 2*Math.PI;
        }
        Ray ray = new Ray(traceAngle);
        ray.setLocation(carLocation);
        Hit hit = ray.hitPixel(pixelData);
        return hit;
    }

    private Hit trace(double angle){
        //todo remove cos and sin by something simpler
        double dx = Math.cos(angle+currentCarAngleRad);
        double dy = Math.sin(angle+currentCarAngleRad);

        //set direction
        Ray ray = new Ray();
        ray.setLocation(carLocation);
        ray.setDirection(dx, dy);

        //find closest intersection
        Hit bestHit = null;
        for(Segment s: segments){
            Hit hit = ray.hit(s);
            if(hit != null) {
                if (bestHit == null || (hit.getTime() > 0 && hit.getTime() < bestHit.getTime())) {
                    bestHit = hit;
                }
            }
        }
        return bestHit;
    }

    public double getCarHeadingDeg(){
        return currentCarAngleRad*180/Math.PI;
    }

    public double[] getCarLocation(){
        return carLocation;
    }

    public ArrayList<Segment> getSegments(){
        return segments;
    }

    public int[][] getPixelData() {return pixelData;}

    public Image getImage(){return image;}

    public int[] getDimensions()
    {
        return new int[] {image.getWidth(),image.getHeight()};
    }

	synchronized public void updateWorld(){
        double current = angleStartRad;

        if(!moveLikeCar) {
            if (up)
                carLocation[1] += speed;
            if (down)
                carLocation[1] -= speed;
            if (left)
                carLocation[0] -= speed;
            if (right)
                carLocation[0] += speed;
        }else{
            if (up) {
                //move in direction
                carLocation[0] += Math.cos(currentCarAngleRad) * speed;
                carLocation[1] += Math.sin(currentCarAngleRad) * speed;
            }
            if (down) {
                carLocation[0] -= Math.cos(currentCarAngleRad) * speed;
                carLocation[1] -= Math.sin(currentCarAngleRad) * speed;
            }
            if (left){
                currentCarAngleRad -= turnSpeed;
            }
            if (right) {
                currentCarAngleRad += turnSpeed;
            }
        }

        //Image: teleport with mouse click
        if(Objects.equals(args, "image"))
        {
            if (image.checkMouseClicked())
            {
                double[] tempLocation = image.getLocationFromMouse();
                if (tempLocation[0] != 0 && tempLocation[1] != 0)
                {
                    System.out.println("Override!");
                    carLocation = tempLocation;
                }
            }
        }

        int i = 0;

        while (i < 1080) {
            //calculate an intersect for each angle
            Hit hit;
            switch (mode)
            {
                case IMAGE:
                    hit = trace(current);
                    break;
                case RANDOM:
                    hit = trace(current);
                    updateSegments(); // Replaces all segments with latest ones
                    //new SDF(segments); // Makes sdformat file todo: make a new .world file without overfloading the system
                    lineComp.update(segments);
                    break;
                default:
                    hit = trace(current);
                    break;
            }

            data[i]=hit.getTime();
            current -= angleDiffRad;
            i++;
        }
    }

	synchronized public void move(KeyEvent e){
        /*
	    switch(e.getKeyChar()){
            case 'z':
                carLocation[1] += 100;
                break;
            case 's':
                carLocation[1] -= 100;
                break;
            case 'q':
                carLocation[0] -= 100;
                break;
            case 'd':
                carLocation[0] += 100;
                break;
        }*/
    }

    synchronized public void setKey(KeyEvent e){
        switch(e.getKeyChar()){
            case 'z':
                up = true;
                break;
            case 's':
                down = true;
                break;
            case 'q':
                left = true;
                break;
            case 'd':
                right = true;
                break;
        }
    }

    synchronized public void removeKey(KeyEvent e){
        switch(e.getKeyChar()){
            case 'z':
                up = false;
                break;
            case 's':
                down = false;
                break;
            case 'q':
                left = false;
                break;
            case 'd':
                right = false;
                break;
        }
    }

    private void getWorldFromImage(){
        image = new Image();
        segments = image.openImage();
        image.getMouse();
    }

    private void getRandomWorld(){
        randomGen = new RandomGen(new Point((int) carLocation[0],(int) carLocation[1]));
        updateSegments(); // Replaces all segments with latest ones
        showWorld();
    }

	private void buildWorld()
    {
        segments.add(new Segment(new double[]{-10, 10},new double[]{ -10, -10}));
        segments.add(new Segment(new double[]{-10, -10},new double[]{ 10, -10}));
        segments.add(new Segment(new double[]{10, -10},new double[]{ 10, 10}));
        segments.add(new Segment(new double[]{10, 10},new double[]{ -10, 10}));

        segments.add(new Segment(new double[]{-6, 6},new double[]{ -6, 2}));
        segments.add(new Segment(new double[]{-6, 2},new double[]{ -2, 2}));
        segments.add(new Segment(new double[]{-2, 2},new double[]{ -2, 6}));
        segments.add(new Segment(new double[]{-2, 6},new double[]{ -6, 6}));

        segments.add(new Segment(new double[]{-4, -3},new double[]{ -5, -4}));
        segments.add(new Segment(new double[]{-5, -4},new double[]{ -4, -5}));
        segments.add(new Segment(new double[]{-4, -5},new double[]{ -3, -4}));
        segments.add(new Segment(new double[]{-3, -4},new double[]{ -4, -3}));

        segments.add(new Segment(new double[]{6, 7},new double[]{ 3, 0}));
        segments.add(new Segment(new double[]{3, 0},new double[]{ 6, -7}));
        segments.add(new Segment(new double[]{6, -7},new double[]{ 6, 7}));
	}
	
	synchronized public String encodeData(){
	    dataString = new StringBuilder();
		for(int j=0; j<1080; j++){
			int value = (int) this.data[j];
            //int value =(int) ((j+300) >> 6);
			char firstChar = (char) (((value & 0x3F000) >> 12) + 0x30);
			char secondChar = (char) (((value & 0xFC0) >> 6) + 0x30);
			char thirdChar =  (char) ((value & 0x3F) + 0x30);
			dataString.append(firstChar);
			dataString.append(secondChar);
			dataString.append(thirdChar);
		}
        return dataString.toString();
	}

    public void updateSegments()
    {
        GridPiece[][] grid = randomGen.checkWorld(new Point((int) carLocation[0],(int) carLocation[1]));
        segments.clear();

        for(GridPiece[] gridPieces : grid)
        {
            for (GridPiece gridPiece : gridPieces)
            {
                if (!gridPiece.segments.isEmpty())
                {
                    segments.addAll(gridPiece.segments);
                }
            }
        }
    }

    private void checkMode()
    {
        switch (args)
        {
            case "image":
                mode = Mode.IMAGE;
                carLocation = new double[] {0,0};
                speed = 0.1; // 10 cm per seconde
                getWorldFromImage();

                break;
            case "random":
                mode = Mode.RANDOM;
                carLocation = new double[] {0,0};
                speed = 0.1;
                getRandomWorld();

                break;
            default:
                mode = Mode.DEBUG;
                carLocation = new double[] {0,0};
                speed = 0.1;
                buildWorld();
                break;
        }
    }

    private void showWorld()
    {
        lineComp = new LineComp(segments);

        testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        Dimension d = new Dimension(800, 800);
        testFrame.setSize(d);
        testFrame.add(lineComp, BorderLayout.CENTER);
        testFrame.setVisible(true);
    }
}

